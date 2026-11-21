package app.skyward.data.location

import app.skyward.domain.FixResult
import app.skyward.domain.GeoPoint
import app.skyward.domain.SavedLocation
import co.touchlab.kermit.Logger
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.CoreLocation.kCLLocationAccuracyReduced
import platform.Foundation.NSError
import platform.Foundation.timeIntervalSince1970
import platform.darwin.NSObject

private val log = Logger.withTag("IosLocationSource")
private val FIX_TIMEOUT = 30.seconds

class IosLocationSource : LocationSource {
    /** CLLocationManager delivers callbacks on the run loop it was created on, so stay on main. */
    override suspend fun currentFix(): FixResult =
        withContext(Dispatchers.Main) {
            withTimeoutOrNull(FIX_TIMEOUT) { requestFix() }
                ?: FixResult.Unavailable.also { log.w { "No location fix within $FIX_TIMEOUT" } }
        }

    @OptIn(ExperimentalForeignApi::class)
    private suspend fun requestFix(): FixResult = suspendCancellableCoroutine { continuation ->
        val manager = CLLocationManager()
        val delegate =
            FixDelegate(manager) { result ->
                if (continuation.isActive) continuation.resume(result)
            }
        // The delegate property is weak; this closure keeps both objects alive until the request
        // ends.
        continuation.invokeOnCancellation { delegate.finish(FixResult.Unavailable) }
        manager.delegate = delegate
        manager.desiredAccuracy = kCLLocationAccuracyReduced
        if (manager.authorizationStatus == kCLAuthorizationStatusNotDetermined) {
            manager.requestWhenInUseAuthorization()
        }
        // Authorization changes, including the initial state, arrive via the delegate.
    }

    private class FixDelegate(
        private val manager: CLLocationManager,
        private val onResult: (FixResult) -> Unit,
    ) : NSObject(), CLLocationManagerDelegateProtocol {
        private var finished = false
        private var requested = false

        fun finish(result: FixResult) {
            if (finished) return
            finished = true
            manager.stopUpdatingLocation()
            manager.delegate = null
            onResult(result)
        }

        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
            when (manager.authorizationStatus) {
                kCLAuthorizationStatusNotDetermined -> Unit
                kCLAuthorizationStatusDenied,
                kCLAuthorizationStatusRestricted -> {
                    log.w { "Location permission denied or restricted" }
                    finish(FixResult.PermissionDenied)
                }
                else ->
                    if (!requested) {
                        requested = true
                        manager.requestLocation()
                    }
            }
        }

        @OptIn(ExperimentalForeignApi::class)
        override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
            val location = didUpdateLocations.lastOrNull() as? CLLocation
            if (location == null) {
                log.w { "Location update contained no location" }
                finish(FixResult.Unavailable)
                return
            }
            val point = location.coordinate.useContents { GeoPoint(latitude, longitude) }
            val fixedAt =
                Instant.fromEpochSeconds(location.timestamp.timeIntervalSince1970.toLong())
            finish(FixResult.Success(SavedLocation(point, fixedAt)))
        }

        override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
            log.w { "Location request failed: ${didFailWithError.localizedDescription}" }
            finish(FixResult.Unavailable)
        }
    }
}
