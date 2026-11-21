@file:Suppress("MatchingDeclarationName")

package app.skyward.sky

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.darwin.NSObject

actual class LocationPermissionRequester
internal constructor(
    private val manager: CLLocationManager,
    private val onResult: (Boolean) -> Unit,
) {
    actual fun request() {
        val status = manager.authorizationStatus
        if (status == kCLAuthorizationStatusNotDetermined) {
            manager.requestWhenInUseAuthorization()
        } else {
            onResult(status.isGranted())
        }
    }

    actual fun openSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(
            url,
            options = emptyMap<Any?, Any>(),
            completionHandler = null,
        )
    }
}

/**
 * The manager and delegate live as long as the composition: CLLocationManager holds its delegate
 * weakly and calls it back on the thread it was created on, which here is main.
 */
@Composable
actual fun rememberLocationPermissionRequester(
    onResult: (granted: Boolean) -> Unit,
): LocationPermissionRequester {
    val currentOnResult by rememberUpdatedState(onResult)
    val manager = remember { CLLocationManager() }
    val delegate =
        remember(manager) {
            AuthorizationDelegate(manager.authorizationStatus) { currentOnResult(it) }
        }
    DisposableEffect(manager, delegate) {
        manager.delegate = delegate
        onDispose { manager.delegate = null }
    }
    return remember(manager) { LocationPermissionRequester(manager) { currentOnResult(it) } }
}

/**
 * Reports each change of answer, wherever the prompt came from. The callback CoreLocation makes
 * when the delegate is set, with the status as it already was, is not a change and is skipped.
 */
private class AuthorizationDelegate(
    private var lastStatus: CLAuthorizationStatus,
    private val onResult: (Boolean) -> Unit,
) : NSObject(), CLLocationManagerDelegateProtocol {
    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        val status = manager.authorizationStatus
        if (status == lastStatus) return
        lastStatus = status
        if (status != kCLAuthorizationStatusNotDetermined) onResult(status.isGranted())
    }
}

private fun CLAuthorizationStatus.isGranted() =
    this == kCLAuthorizationStatusAuthorizedWhenInUse ||
        this == kCLAuthorizationStatusAuthorizedAlways
