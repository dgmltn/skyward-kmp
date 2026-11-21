package app.skyward.widget

import app.skyward.data.sky.KastroSkyRepository
import app.skyward.dial.buildDialModel
import app.skyward.domain.GeoPoint
import app.skyward.domain.SavedLocation
import app.skyward.domain.SkyDay
import app.skyward.domain.SkyNow
import app.skyward.domain.SkyRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

private class CountingSkyRepository(private val delegate: SkyRepository) : SkyRepository {
    var dayCalls = 0
        private set

    override fun day(point: GeoPoint, date: LocalDate, timeZone: TimeZone): SkyDay {
        dayCalls++
        return delegate.day(point, date, timeZone)
    }

    override fun now(point: GeoPoint, instant: Instant): SkyNow = delegate.now(point, instant)
}

class WidgetTimelineBuilderTest {
    private val sky = CountingSkyRepository(KastroSkyRepository())
    private val builder = WidgetTimelineBuilder(sky)
    private val london =
        SavedLocation(GeoPoint(51.5074, -0.1278), Instant.parse("2026-06-01T00:00:00Z"))
    private val zone = "Europe/London"

    private fun entries(now: String): List<WidgetEntry> {
        val timeline =
            builder.build(london, Instant.parse(now).toEpochMilliseconds(), zone, use24Hour = true)
        return assertIs<WidgetTimeline.Entries>(timeline).entries
    }

    @Test
    fun noLocationGivesNoLocation() {
        val timeline = builder.build(null, 0L, zone, use24Hour = true)
        assertEquals(WidgetTimeline.NoLocation, timeline)
    }

    @Test
    fun seventyTwoEntriesFiveMinutesApart() {
        val now = Instant.parse("2026-06-21T10:00:00Z")
        val entries = entries("2026-06-21T10:00:00Z")
        assertEquals(72, entries.size)
        assertEquals(now.toEpochMilliseconds(), entries.first().epochMillis)
        entries.zipWithNext { a, b -> assertEquals(300_000L, b.epochMillis - a.epochMillis) }
    }

    @Test
    fun entriesAfterMidnightUseNextDay() {
        val entries = entries("2026-06-21T21:30:00Z")
        val beforeMidnight = entries.first { it.epochMillis == millis("2026-06-21T22:55:00Z") }
        val midnight = entries.first { it.epochMillis == millis("2026-06-21T23:00:00Z") }

        val lateHand = assertNotNull(beforeMidnight.dial.hand)
        assertTrue(lateHand.angleDegrees in 438.0..442.0, "was ${lateHand.angleDegrees}")

        val earlyHand = assertNotNull(midnight.dial.hand)
        assertEquals(100.0, earlyHand.angleDegrees, 0.01)
        val nextDay =
            KastroSkyRepository().day(london.point, LocalDate(2026, 6, 22), TimeZone.of(zone))
        val expected = buildDialModel(nextDay, null, null, use24Hour = true)
        assertEquals(
            expected.arcs.map { it.startDegrees },
            midnight.dial.arcs.map { it.startDegrees },
        )
    }

    @Test
    fun dayComputedAtMostTwicePerTimeline() {
        entries("2026-06-21T21:30:00Z")
        assertTrue(sky.dayCalls <= 2, "day() called ${sky.dayCalls} times")
    }

    private fun millis(iso: String) = Instant.parse(iso).toEpochMilliseconds()

    @Test
    fun unknownTimeZoneFallsBackToSystemZone() {
        val now = Instant.parse("2026-06-21T10:00:00Z")
        val timeline =
            builder.build(london, now.toEpochMilliseconds(), "Not/AZone", use24Hour = true)

        val entries = assertIs<WidgetTimeline.Entries>(timeline).entries
        assertEquals(72, entries.size)
    }

    @Test
    fun throwingBuildGivesUnavailable() {
        val timeline = timelineOrUnavailable { error("Sky calculation failed") }
        assertEquals(WidgetTimeline.Unavailable, timeline)
    }

    @Test
    fun successfulBuildPassesThrough() {
        val timeline = timelineOrUnavailable { WidgetTimeline.NoLocation }
        assertEquals(WidgetTimeline.NoLocation, timeline)
    }
}
