package app.skyward.data.sky

import app.skyward.domain.GeoPoint
import app.skyward.domain.SkyDay
import dev.jamesyox.kastro.sol.SolarEvent
import dev.jamesyox.kastro.sol.SolarPhase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atDate
import kotlinx.datetime.toInstant

class KastroSkyRepositoryTest {
    private val repository = KastroSkyRepository()

    private val london = GeoPoint(latitude = 51.5074, longitude = -0.1278)
    private val londonZone = TimeZone.of("Europe/London")
    private val tromso = GeoPoint(latitude = 69.6492, longitude = 18.9553)
    private val oslo = TimeZone.of("Europe/Oslo")
    private val denver = GeoPoint(latitude = 39.7392, longitude = -104.9903)
    private val denverZone = TimeZone.of("America/Denver")

    private val tolerance = 3.minutes

    @Test
    fun londonSolsticeSunriseAndSunset() {
        val day = repository.day(london, LocalDate(2026, 6, 21), londonZone)

        val sunrise = day.solarEvents.filterIsInstance<SolarEvent.Sunrise>().single()
        val sunset = day.solarEvents.filterIsInstance<SolarEvent.Sunset>().single()

        assertNear(localInstant(LocalDate(2026, 6, 21), 4, 43, londonZone), sunrise.time)
        assertNear(localInstant(LocalDate(2026, 6, 21), 21, 21, londonZone), sunset.time)
    }

    @Test
    fun phasesAreContiguousAndCoverTheDay() {
        listOf(LocalDate(2026, 6, 21), LocalDate(2026, 12, 21)).forEach { date ->
            val day = repository.day(london, date, londonZone)

            assertCovers(day, day.solarPhases.map { it.start to it.end })
            assertCovers(day, day.moonSpans.map { it.start to it.end })
        }
    }

    @Test
    fun polarDayIsOneDaySpan() {
        val day = repository.day(tromso, LocalDate(2026, 6, 21), oslo)

        assertIs<SolarPhase.Day>(day.solarPhases.single().phase)
        assertTrue(day.solarEvents.none { it is SolarEvent.Sunrise || it is SolarEvent.Sunset })
    }

    @Test
    fun polarNightHasNoDay() {
        val day = repository.day(tromso, LocalDate(2026, 12, 21), oslo)

        assertTrue(day.solarPhases.none { it.phase is SolarPhase.Day })
        assertCovers(day, day.solarPhases.map { it.start to it.end })
    }

    @Test
    fun dstSpringDayIs23Hours() {
        val day = repository.day(denver, LocalDate(2026, 3, 8), denverZone)

        assertEquals(23.hours, day.end - day.start)
    }

    @Test
    fun dstFallDayIs25Hours() {
        val day = repository.day(denver, LocalDate(2026, 11, 1), denverZone)

        assertEquals(25.hours, day.end - day.start)
    }

    @Test
    fun moonSpansCoverEveryDayOfAMonth() {
        (1..31).forEach { dayOfMonth ->
            val day = repository.day(london, LocalDate(2026, 1, dayOfMonth), londonZone)

            assertCovers(day, day.moonSpans.map { it.start to it.end })
        }
    }

    @Test
    fun nowReportsNextEventsAndPhase() {
        val now = repository.now(london, Instant.parse("2026-06-21T12:00:00Z"))

        assertNear(Instant.parse("2026-06-21T20:21:00Z"), assertNotNull(now.nextSunset))
        assertTrue(now.moonPhaseDegrees in 0.0..<360.0)
        assertIs<SolarPhase.Day>(now.solarPhase)
    }

    @Test
    fun nowInPolarDayCompletes() {
        val start = Instant.parse("2026-06-21T12:00:00Z")

        val now = repository.now(tromso, start)

        now.nextSunset?.let { assertTrue(it > start) }
    }

    private fun localInstant(date: LocalDate, hour: Int, minute: Int, zone: TimeZone): Instant =
        LocalTime(hour, minute).atDate(date).toInstant(zone)

    private fun assertNear(expected: Instant, actual: Instant) {
        val difference = (expected - actual).absoluteValue
        assertTrue(difference <= tolerance, "expected $expected but was $actual")
    }

    private fun assertCovers(day: SkyDay, spans: List<Pair<Instant, Instant>>) {
        assertEquals(day.start, spans.first().first)
        assertEquals(day.end, spans.last().second)
        spans.zipWithNext().forEach { (current, next) -> assertEquals(current.second, next.first) }
    }
}
