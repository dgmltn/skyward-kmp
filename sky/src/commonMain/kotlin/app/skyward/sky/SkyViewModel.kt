package app.skyward.sky

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.skyward.dial.buildDialModel
import app.skyward.dial.hitTest
import app.skyward.domain.FixResult
import app.skyward.domain.GeoPoint
import app.skyward.domain.LocationRepository
import app.skyward.domain.SkyDay
import app.skyward.domain.SkyItem
import app.skyward.domain.SkyNow
import app.skyward.domain.SkyRepository
import co.touchlab.kermit.Logger
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class SkyViewModel(
    private val location: LocationRepository,
    private val sky: SkyRepository,
    private val clock: Clock,
    private val timeZone: () -> TimeZone,
    ticker: Flow<Unit>,
    private val use24Hour: () -> Boolean,
) : ViewModel() {
    private val selected = MutableStateFlow<SkyItem?>(null)
    private val tab = MutableStateFlow(SkyTab.STATES)
    private val permissionDenied = MutableStateFlow(false)
    private val fix = MutableStateFlow(FixProgress())
    private val foregroundCount = MutableStateFlow(0)

    private var cachedDay: CachedDay? = null
    private var lastSkyData: SkyData? = null
    private var lastReady: SkyUiState.Ready? = null

    private val skyData: Flow<SkySource> =
        combine(location.saved, merge(ticker, foregroundCount.map {})) { saved, _ ->
            if (saved == null) SkySource.NoLocation else skySourceAt(saved.point)
        }

    val state: StateFlow<SkyUiState> =
        combine(skyData, selected, tab, permissionDenied, fix) { source, selected, tab, denied, fix
                ->
                when (source) {
                    SkySource.NoLocation ->
                        SkyUiState.NeedsLocation(
                            denied = denied,
                            locating = fix.locating,
                            couldNotLocate = fix.failed,
                        )
                    SkySource.Failed -> SkyUiState.Unavailable
                    is SkySource.Computed -> readyOrFallback(source.data, selected, tab)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), SkyUiState.Loading)

    fun onForeground() {
        foregroundCount.update { it + 1 }
        refreshLocation()
    }

    fun onPermissionResult(granted: Boolean) {
        permissionDenied.value = !granted
        if (granted) refreshLocation()
    }

    fun onDialTap(x: Double, y: Double) {
        val ready = state.value as? SkyUiState.Ready ?: return
        onSelect(ready.dial.hitTest(x, y))
    }

    fun onSelect(item: SkyItem?) {
        selected.update { current -> if (item == current) null else item }
    }

    fun onTabSelected(tab: SkyTab) {
        this.tab.value = tab
    }

    @Suppress("TooGenericExceptionCaught")
    private fun refreshLocation() {
        viewModelScope.launch {
            fix.update { it.copy(locating = true) }
            val located =
                try {
                    location.refresh() is FixResult.Success
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    log.e(e) { "Couldn't refresh the location" }
                    false
                }
            if (!located) log.w { "No location from this refresh" }
            fix.value = FixProgress(locating = false, failed = !located)
        }
    }

    /** Falls back to the last sky that was worked out when the calculation throws. */
    @Suppress("TooGenericExceptionCaught")
    private fun skySourceAt(point: GeoPoint): SkySource {
        val data =
            try {
                skyAt(point).also { lastSkyData = it }
            } catch (e: Exception) {
                log.e(e) { "Couldn't work out the sky" }
                lastSkyData
            }
        return data?.let(SkySource::Computed) ?: SkySource.Failed
    }

    @Suppress("TooGenericExceptionCaught")
    private fun readyOrFallback(data: SkyData, selected: SkyItem?, tab: SkyTab): SkyUiState =
        try {
            val item = selected?.takeIf { data.day.contains(it) }
            val twentyFourHour = use24Hour()
            SkyUiState.Ready(
                    day = data.day,
                    now = data.now,
                    dial = buildDialModel(data.day, data.now, item, twentyFourHour),
                    selected = item,
                    tab = tab,
                    use24Hour = twentyFourHour,
                )
                .also { lastReady = it }
        } catch (e: Exception) {
            log.e(e) { "Couldn't build the dial" }
            lastReady ?: SkyUiState.Unavailable
        }

    private fun skyAt(point: GeoPoint): SkyData {
        val instant = clock.now()
        val zone = timeZone()
        val date = instant.toLocalDateTime(zone).date
        val key = DayKey(point, date, zone)
        val day =
            cachedDay?.takeIf { it.key == key }?.day
                ?: sky.day(point, date, zone).also { cachedDay = CachedDay(key, it) }
        return SkyData(day, sky.now(point, instant))
    }

    private data class DayKey(val point: GeoPoint, val date: LocalDate, val timeZone: TimeZone)

    private class CachedDay(val key: DayKey, val day: SkyDay)

    private class SkyData(val day: SkyDay, val now: SkyNow)

    private data class FixProgress(val locating: Boolean = false, val failed: Boolean = false)

    private sealed interface SkySource {
        data object NoLocation : SkySource

        data object Failed : SkySource

        class Computed(val data: SkyData) : SkySource
    }
}

private val log = Logger.withTag("SkyViewModel")

private fun SkyDay.contains(item: SkyItem): Boolean =
    when (item) {
        is SkyItem.Solar -> item.span in solarPhases
        is SkyItem.Light -> item.span in lightSpans
        is SkyItem.Moon -> item.span in moonSpans
        is SkyItem.SolarEventItem -> item.event in solarEvents
        is SkyItem.LunarEventItem -> item.event in lunarEvents
    }
