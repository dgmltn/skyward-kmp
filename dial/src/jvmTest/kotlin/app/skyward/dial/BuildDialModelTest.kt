package app.skyward.dial

import app.skyward.data.sky.KastroSkyRepository
import app.skyward.domain.GeoPoint
import app.skyward.domain.SkyDay
import app.skyward.domain.SkyItem
import app.skyward.domain.SkyNow
import dev.jamesyox.kastro.sol.SolarEvent
import dev.jamesyox.kastro.sol.SolarPhase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class BuildDialModelTest {
    private val repository = KastroSkyRepository()

    private val london = GeoPoint(51.5074, -0.1278)
    private val londonZone = TimeZone.of("Europe/London")
    private val tromso = GeoPoint(69.6492, 18.9553)
    private val oslo = TimeZone.of("Europe/Oslo")
    private val denver = GeoPoint(39.7392, -104.9903)
    private val denverZone = TimeZone.of("America/Denver")

    private val solstice = LocalDate(2026, 6, 21)
    private val londonDay = repository.day(london, solstice, londonZone)

    private fun dial(
        day: SkyDay = londonDay,
        now: SkyNow? = null,
        selected: SkyItem? = null,
        use24Hour: Boolean = false,
    ) = buildDialModel(day, now, selected, use24Hour)

    private fun londonAt(hour: Int) =
        LocalDateTime(solstice, LocalTime(hour, 0)).toInstant(londonZone)

    @Test
    fun midnightIsJustRightOfBottomAndNoonIsTop() {
        val start = londonDay.start
        val end = londonDay.end

        assertEquals(100.0, DialGeometry.angleOf(start, start, end), 0.01)
        assertEquals(440.0, DialGeometry.angleOf(end, start, end), 0.01)
        assertEquals(270.0, DialGeometry.angleOf(londonAt(12), start, end), 0.01)
    }

    @Test
    fun instantAtRoundTripsAndRejectsWedge() {
        val start = londonDay.start
        val end = londonDay.end
        val t = londonAt(15) + 37.seconds

        val back = DialGeometry.instantAt(DialGeometry.angleOf(t, start, end), start, end)

        assertNotNull(back)
        assertTrue((back - t).absoluteValue < 1.seconds)
        assertNull(DialGeometry.instantAt(90.0, start, end))
    }

    @Test
    fun dstDayHas23HourLabelsWithCorrectText() {
        val spring = repository.day(denver, LocalDate(2026, 3, 8), denverZone)
        val labels = dial(spring).hourLabels.map { it.text }

        assertEquals(24, labels.size)
        assertEquals(listOf("12am", "1am", "3am", "4am"), labels.take(4))
        assertEquals(listOf("11pm", "12am"), labels.takeLast(2))
        assertEquals("12pm", labels[11])
    }

    @Test
    fun fallBackDayDoublesTheOneToTwoAmGap() {
        val fall = repository.day(denver, LocalDate(2026, 11, 1), denverZone)
        val labels = dial(fall).hourLabels

        assertEquals(25, labels.size)
        assertEquals(labels.map { it.text }.distinct().size, 24)
        assertEquals("1am", labels[1].text)
        assertEquals("2am", labels[2].text)
        val doubled = labels[2].angleDegrees - labels[1].angleDegrees
        val normal = labels[4].angleDegrees - labels[3].angleDegrees
        assertEquals(2 * normal, doubled, 0.001)
    }

    @Test
    fun hourTicksStraddleTheHourRingLikeTheDemo() {
        val ticks = dial().hourTicks

        assertEquals(25, ticks.size)
        assertTrue(ticks.all { it.innerRadius == 85.5 && it.outerRadius == 90.5 })
        assertTrue(ticks.all { it.width == 0.4 && it.color == DialPalette.Hour })
    }

    @Test
    fun twentyFourHourLabelsArePadded() {
        val labels = dial(use24Hour = true).hourLabels.map { it.text }

        assertEquals(25, labels.size)
        assertEquals(listOf("00", "01", "02"), labels.take(3))
        assertEquals(listOf("23", "00"), labels.takeLast(2))
    }

    @Test
    fun arcsCoverEachRing() {
        val arcs = dial().arcs
        fun sweep(radius: Double) = arcs.filter { it.radius == radius }.sumOf { it.sweepDegrees }

        assertEquals(340.0, sweep(DialGeometry.SOLAR_RADIUS), 0.01)
        assertEquals(340.0, sweep(DialGeometry.MOON_RADIUS), 0.01)
        val light = arcs.filter { it.item is SkyItem.Light }
        assertTrue(light.isNotEmpty())
        assertTrue(
            light.all { it.color == DialPalette.GoldenHour || it.color == DialPalette.BlueHour },
        )
        assertEquals(
            setOf(DialGeometry.SOLAR_RADIUS, DialGeometry.MOON_RADIUS) +
                light.map { it.radius }.toSet(),
            arcs.map { it.radius }.toSet(),
        )
    }

    @Test
    fun polarDayDrawsOneFullSolarArc() {
        val day = repository.day(tromso, solstice, oslo)
        val solar = dial(day).arcs.filter { it.radius == DialGeometry.SOLAR_RADIUS }

        val arc = solar.single()
        assertEquals(340.0, arc.sweepDegrees, 0.01)
        assertEquals(DialPalette.Day, arc.color)
    }

    @Test
    fun selectionDimsOthers() {
        val selected = SkyItem.Solar(londonDay.solarPhases[1])
        val arcs = dial(selected = selected).arcs

        assertEquals(1.0f, arcs.single { it.item == selected }.alpha)
        assertTrue(arcs.filter { it.item != selected }.all { it.alpha == 0.3f })
        assertTrue(dial().arcs.all { it.alpha == 1.0f })
    }

    @Test
    fun selectedEventDrawsRedTickFromCentre() {
        val sunrise = londonDay.solarEvents.filterIsInstance<SolarEvent.Sunrise>().single()
        val model = dial(selected = SkyItem.SolarEventItem(sunrise))

        val tick = assertNotNull(model.selectedEventTick)
        assertEquals(25.0, tick.innerRadius)
        assertEquals(86.5, tick.outerRadius)
        assertEquals(0.5, tick.width)
        assertEquals(DialPalette.Hand, tick.color)
        assertEquals(
            DialGeometry.angleOf(sunrise.time, londonDay.start, londonDay.end),
            tick.angleDegrees,
            0.001,
        )
        assertTrue(model.arcs.all { it.alpha == 1.0f })
        assertNull(dial().selectedEventTick)
    }

    @Test
    fun markersOnlyForDrawnEvents() {
        val markers = dial().markers
        val drawnSolar =
            londonDay.solarEvents.filter {
                it is SolarEvent.Sunrise ||
                    it is SolarEvent.Sunset ||
                    it is SolarEvent.Noon ||
                    it is SolarEvent.Nadir
            }

        assertTrue(londonDay.solarEvents.size > drawnSolar.size)
        assertEquals(drawnSolar.size + londonDay.lunarEvents.size, markers.size)
        val solarItems = markers.map { it.item }.filterIsInstance<SkyItem.SolarEventItem>()
        assertEquals(drawnSolar.toSet(), solarItems.map { it.event }.toSet())
        assertEquals(
            londonDay.lunarEvents.toSet(),
            markers
                .map { it.item }
                .filterIsInstance<SkyItem.LunarEventItem>()
                .map { it.event }
                .toSet(),
        )
    }

    @Test
    fun handAndMoonIconFollowNow() {
        val instant = LocalDateTime(solstice, LocalTime(18, 0)).toInstant(londonZone)
        val now = repository.now(london, instant)

        val model = dial(now = now)

        val expected = DialGeometry.angleOf(instant, londonDay.start, londonDay.end)
        assertEquals(expected, assertNotNull(model.hand).angleDegrees, 0.001)
        assertEquals(now.moonPhaseDegrees, assertNotNull(model.moonIcon).phaseDegrees)
        val empty = dial(now = null)
        assertNull(empty.hand)
        assertNull(empty.moonIcon)
    }

    @Test
    fun solarPhaseColourFollowsTwilightSubtype() {
        assertEquals(DialPalette.Night, DialPalette.of(SolarPhase.Night))
    }
}
