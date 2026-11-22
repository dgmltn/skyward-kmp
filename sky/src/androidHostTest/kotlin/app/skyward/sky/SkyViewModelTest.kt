@file:Suppress("MagicNumber", "TooManyFunctions")

package app.skyward.sky

import app.skyward.data.sky.KastroSkyRepository
import app.skyward.dial.DialGeometry
import app.skyward.domain.FixResult
import app.skyward.domain.GeoPoint
import app.skyward.domain.LocationRepository
import app.skyward.domain.SavedLocation
import app.skyward.domain.SkyDay
import app.skyward.domain.SkyItem
import app.skyward.domain.SkyNow
import app.skyward.domain.SkyRepository
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

@OptIn(ExperimentalCoroutinesApi::class)
class SkyViewModelTest {
    private val london = SavedLocation(GeoPoint(51.5074, -0.1278), Instant.fromEpochMilliseconds(0))
    private val denver =
        SavedLocation(GeoPoint(39.7392, -104.9903), Instant.fromEpochMilliseconds(0))
    private val londonZone = TimeZone.of("Europe/London")
    private val denverZone = TimeZone.of("America/Denver")

    private class FakeLocation(initial: SavedLocation?) : LocationRepository {
        val savedState = MutableStateFlow(initial)
        override val saved: Flow<SavedLocation?> = savedState
        val refreshes = MutableStateFlow(0)

        override suspend fun refresh(): FixResult {
            refreshes.update { it + 1 }
            return FixResult.Unavailable
        }
    }

    private class ThrowingLocation(initial: SavedLocation?) : LocationRepository {
        override val saved: Flow<SavedLocation?> = MutableStateFlow(initial)
        val refreshes = MutableStateFlow(0)

        override suspend fun refresh(): FixResult {
            refreshes.update { it + 1 }
            throw SecurityException("Location permission revoked")
        }
    }

    private class FlakySkyRepository : SkyRepository {
        private val delegate = KastroSkyRepository()
        var failing = false

        override fun day(point: GeoPoint, date: LocalDate, timeZone: TimeZone): SkyDay {
            check(!failing) { "Sky calculation failed" }
            return delegate.day(point, date, timeZone)
        }

        override fun now(point: GeoPoint, instant: Instant): SkyNow {
            check(!failing) { "Sky calculation failed" }
            return delegate.now(point, instant)
        }
    }

    private class FakeClock(var current: Instant) : Clock {
        override fun now(): Instant = current
    }

    private val ticker = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private var zone = londonZone

    private fun viewModel(
        location: LocationRepository,
        clock: Clock = FakeClock(Instant.parse("2026-06-21T10:00:00Z")),
        sky: SkyRepository = KastroSkyRepository(),
    ) =
        SkyViewModel(
            location = location,
            sky = sky,
            clock = clock,
            timeZone = { zone },
            ticker = ticker,
            use24Hour = { true },
        )

    private suspend fun SkyViewModel.awaitReady(
        condition: (SkyUiState.Ready) -> Boolean = { true }
    ): SkyUiState.Ready =
        state.first { it is SkyUiState.Ready && condition(it) } as SkyUiState.Ready

    @Test
    fun startsLoading() = runTest {
        assertEquals(SkyUiState.Loading, viewModel(FakeLocation(null)).state.value)
    }

    @Test
    fun noSavedLocationShowsNeedsLocationBeforeAnyAnswer() = runTest {
        val viewModel = viewModel(FakeLocation(null))

        assertEquals(
            SkyUiState.NeedsLocation(denied = false),
            viewModel.state.first { it !is SkyUiState.Loading },
        )
    }

    @Test
    fun noSavedLocationAndDeniedShowsNeedsLocationDenied() = runTest {
        val viewModel = viewModel(FakeLocation(null))
        viewModel.state.first { it is SkyUiState.NeedsLocation }

        viewModel.onPermissionResult(granted = false)

        assertEquals(
            SkyUiState.NeedsLocation(denied = true),
            viewModel.state.first { it == SkyUiState.NeedsLocation(denied = true) },
        )
    }

    @Test
    fun grantedPermissionRefreshesLocation() = runTest {
        val location = FakeLocation(null)
        val viewModel = viewModel(location)
        viewModel.state.first { it is SkyUiState.NeedsLocation }

        viewModel.onPermissionResult(granted = true)

        location.refreshes.first { it == 1 }
    }

    @Test
    fun failedFixWithoutSavedLocationSaysSo() = runTest {
        val viewModel = viewModel(FakeLocation(null))
        viewModel.state.first { it is SkyUiState.NeedsLocation }

        viewModel.onPermissionResult(granted = true)

        assertEquals(
            SkyUiState.NeedsLocation(denied = false, couldNotLocate = true),
            viewModel.state.first { (it as? SkyUiState.NeedsLocation)?.couldNotLocate == true },
        )
    }

    @Test
    fun locatingIsShownWhileTheFixIsPending() = runTest {
        val answer = CompletableDeferred<FixResult>()
        val location =
            object : LocationRepository {
                override val saved: Flow<SavedLocation?> = MutableStateFlow(null)

                override suspend fun refresh(): FixResult = answer.await()
            }
        val viewModel = viewModel(location)
        viewModel.state.first { it is SkyUiState.NeedsLocation }

        viewModel.onPermissionResult(granted = true)

        viewModel.state.first { (it as? SkyUiState.NeedsLocation)?.locating == true }
        answer.complete(FixResult.Unavailable)
        assertEquals(
            SkyUiState.NeedsLocation(denied = false, couldNotLocate = true),
            viewModel.state.first { (it as? SkyUiState.NeedsLocation)?.locating == false },
        )
    }

    @Test
    fun aSecondRefreshWaitsForTheOneUnderway() = runTest {
        val answer = CompletableDeferred<FixResult>()
        val refreshes = MutableStateFlow(0)
        val location =
            object : LocationRepository {
                override val saved: Flow<SavedLocation?> = MutableStateFlow(null)

                override suspend fun refresh(): FixResult {
                    refreshes.update { it + 1 }
                    return answer.await()
                }
            }
        val viewModel = viewModel(location)
        viewModel.state.first { it is SkyUiState.NeedsLocation }

        viewModel.onForeground()
        viewModel.state.first { (it as? SkyUiState.NeedsLocation)?.locating == true }
        viewModel.onForeground()
        answer.complete(FixResult.Unavailable)
        viewModel.state.first { (it as? SkyUiState.NeedsLocation)?.locating == false }

        assertEquals(1, refreshes.value)
    }

    @Test
    fun foregroundRefreshesLocation() = runTest {
        val location = FakeLocation(london)
        val viewModel = viewModel(location)
        viewModel.awaitReady()

        viewModel.onForeground()

        location.refreshes.first { it == 1 }
    }

    @Test
    fun savedLocationShowsReadyForToday() = runTest {
        val ready = viewModel(FakeLocation(london)).awaitReady()

        assertEquals(LocalDate(2026, 6, 21), ready.day.date)
        assertEquals(londonZone, ready.day.timeZone)
        assertEquals(SkyTab.STATES, ready.tab)
        assertNull(ready.selected)
    }

    @Test
    fun tickAfterMidnightSwitchesDay() = runTest {
        val clock = FakeClock(Instant.parse("2026-06-21T10:00:00Z"))
        val viewModel = viewModel(FakeLocation(london), clock)
        viewModel.awaitReady()

        clock.current = Instant.parse("2026-06-21T23:01:00Z")
        ticker.emit(Unit)

        val ready = viewModel.awaitReady { it.day.date == LocalDate(2026, 6, 22) }
        assertEquals(clock.current, ready.now.instant)
    }

    @Test
    fun timeZoneChangeOnForegroundRecomputes() = runTest {
        val viewModel = viewModel(FakeLocation(london))
        viewModel.awaitReady()

        zone = denverZone
        viewModel.onForeground()

        viewModel.awaitReady { it.day.timeZone == denverZone }
    }

    @Test
    fun tapSelectsAndSecondTapClears() = runTest {
        val viewModel = viewModel(FakeLocation(london))
        val first = viewModel.awaitReady()
        val noon = LocalDateTime(LocalDate(2026, 6, 21), LocalTime(12, 0)).toInstant(londonZone)
        val radians =
            Math.toRadians(DialGeometry.angleOf(noon, first.day.start, first.day.end))

        viewModel.onDialTap(
            DialGeometry.SOLAR_RADIUS * cos(radians),
            DialGeometry.SOLAR_RADIUS * sin(radians),
        )

        val selected = viewModel.awaitReady { it.selected != null }
        val item = assertIs<SkyItem.Solar>(selected.selected)
        assertTrue(selected.dial.arcs.any { it.item == item && it.alpha == 1f })
        assertTrue(selected.dial.arcs.any { it.item != item && it.alpha < 1f })

        viewModel.onSelect(item)

        val cleared = viewModel.awaitReady { it.selected == null }
        assertTrue(cleared.dial.arcs.all { it.alpha == 1f })
    }

    @Test
    fun tabSelectionIsKept() = runTest {
        val viewModel = viewModel(FakeLocation(london))
        viewModel.awaitReady()

        viewModel.onTabSelected(SkyTab.EVENTS)

        assertEquals(SkyTab.EVENTS, viewModel.awaitReady { it.tab == SkyTab.EVENTS }.tab)
    }

    @Test
    fun newFixRecomputes() = runTest {
        val location = FakeLocation(london)
        val viewModel = viewModel(location)
        val before = viewModel.awaitReady()

        location.savedState.value = denver

        val after = viewModel.awaitReady { it.day != before.day }
        assertEquals(before.day.date, after.day.date)
    }

    @Test
    fun skyCalculationFailingWithoutEarlierDataShowsUnavailable() = runTest {
        val sky = FlakySkyRepository().apply { failing = true }
        val viewModel = viewModel(FakeLocation(london), sky = sky)

        assertEquals(
            SkyUiState.Unavailable,
            viewModel.state.first { it !is SkyUiState.Loading },
        )
    }

    @Test
    fun skyCalculationFailingAfterSuccessKeepsLastDay() = runTest {
        val sky = FlakySkyRepository()
        val clock = FakeClock(Instant.parse("2026-06-21T10:00:00Z"))
        val viewModel = viewModel(FakeLocation(london), clock, sky)
        val before = viewModel.awaitReady()

        sky.failing = true
        clock.current = Instant.parse("2026-06-21T23:01:00Z")
        ticker.emit(Unit)
        viewModel.onTabSelected(SkyTab.EVENTS)

        val after = viewModel.awaitReady { it.tab == SkyTab.EVENTS }
        assertEquals(before.day, after.day)
    }

    @Test
    fun refreshThrowingKeepsReady() = runTest {
        val location = ThrowingLocation(london)
        val viewModel = viewModel(location)
        val before = viewModel.awaitReady()

        viewModel.onForeground()
        location.refreshes.first { it == 1 }
        testScheduler.advanceUntilIdle()

        assertEquals(before, viewModel.state.value)
    }
}
