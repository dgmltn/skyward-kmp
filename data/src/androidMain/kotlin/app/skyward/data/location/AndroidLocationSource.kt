package app.skyward.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat

import app.skyward.domain.FixResult
import app.skyward.domain.GeoPoint
import app.skyward.domain.SavedLocation
import co.touchlab.kermit.Logger
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

class AndroidLocationSource(private val context: Context) : LocationSource {
    override suspend fun currentFix(): FixResult {
        val granted =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        if (!granted) log.w { "ACCESS_COARSE_LOCATION not granted" }
        return if (granted) fixFromProvider() else FixResult.PermissionDenied
    }

    private suspend fun fixFromProvider(): FixResult {
        val manager = context.getSystemService(LocationManager::class.java)
        val provider = manager.coarseProvider()
        val location =
            provider?.let { withTimeoutOrNull(FIX_TIMEOUT) { manager.awaitCurrentLocation(it) } }
        return if (location == null) {
            log.w { "No coarse location from provider $provider within $FIX_TIMEOUT" }
            FixResult.Unavailable
        } else {
            FixResult.Success(
                SavedLocation(
                    point = GeoPoint(location.latitude, location.longitude),
                    fixedAt = Instant.fromEpochMilliseconds(location.time),
                )
            )
        }
    }

    private fun LocationManager.coarseProvider(): String? =
        listOf(LocationManager.FUSED_PROVIDER, LocationManager.NETWORK_PROVIDER).firstOrNull {
            isProviderEnabled(it)
        }

    private suspend fun LocationManager.awaitCurrentLocation(provider: String): Location? =
        suspendCancellableCoroutine { continuation ->
            val signal = CancellationSignal()
            continuation.invokeOnCancellation { signal.cancel() }
            LocationManagerCompat.getCurrentLocation(
                this,
                provider,
                signal,
                Executor { it.run() },
            ) { location ->
                continuation.resume(location)
            }
        }

    private companion object {
        val log = Logger.withTag("AndroidLocationSource")
        val FIX_TIMEOUT = 30.seconds
    }
}
