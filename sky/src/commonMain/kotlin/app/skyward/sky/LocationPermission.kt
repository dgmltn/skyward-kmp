@file:Suppress("MatchingDeclarationName")

package app.skyward.sky

import androidx.compose.runtime.Composable

/** Asks for approximate location, or sends the user to Settings once they've said no. */
expect class LocationPermissionRequester {
    /** Shows the system prompt, or reports the existing answer if the user already gave one. */
    fun request()

    /** Opens Skyward's page in the system Settings app. */
    fun openSettings()
}

/** A requester that reports each answer to [onResult]. */
@Composable
expect fun rememberLocationPermissionRequester(
    onResult: (granted: Boolean) -> Unit,
): LocationPermissionRequester
