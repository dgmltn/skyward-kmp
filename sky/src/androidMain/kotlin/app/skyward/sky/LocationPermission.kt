@file:Suppress("MatchingDeclarationName")

package app.skyward.sky

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

actual class LocationPermissionRequester
internal constructor(
    private val launcher: ManagedActivityResultLauncher<String, Boolean>,
    private val context: Context,
) {
    actual fun request() {
        launcher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    actual fun openSettings() {
        val uri = Uri.fromParts("package", context.packageName, null)
        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri))
    }
}

@Composable
actual fun rememberLocationPermissionRequester(
    onResult: (granted: Boolean) -> Unit,
): LocationPermissionRequester {
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission(), onResult)
    val context = LocalContext.current
    return remember(launcher, context) { LocationPermissionRequester(launcher, context) }
}
