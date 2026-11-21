package app.skyward.sky

import app.skyward.dial.DialModel
import app.skyward.domain.SkyDay
import app.skyward.domain.SkyItem
import app.skyward.domain.SkyNow

sealed interface SkyUiState {
    data object Loading : SkyUiState

    data class NeedsLocation(val denied: Boolean) : SkyUiState

    /** There's a location but the sky couldn't be worked out for it. */
    data object Unavailable : SkyUiState

    data class Ready(
        val day: SkyDay,
        val now: SkyNow,
        val dial: DialModel,
        val selected: SkyItem?,
        val tab: SkyTab,
        val use24Hour: Boolean,
    ) : SkyUiState
}
