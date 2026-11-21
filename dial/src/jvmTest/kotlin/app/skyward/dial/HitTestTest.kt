package app.skyward.dial

import app.skyward.data.sky.KastroSkyRepository
import app.skyward.domain.GeoPoint
import app.skyward.domain.SkyItem
import dev.jamesyox.kastro.sol.LightState
import dev.jamesyox.kastro.sol.SolarEvent
import dev.jamesyox.kastro.sol.SolarPhase
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class HitTestTest {
    private val zone = TimeZone.of("Europe/London")
    private val date = LocalDate(2026, 6, 21)
    private val day = KastroSkyRepository().day(GeoPoint(51.5074, -0.1278), date, zone)
    private val model = buildDialModel(day, now = null, selected = null, use24Hour = false)

    private fun angle(instant: Instant) = DialGeometry.angleOf(instant, day.start, day.end)

    private fun hit(radius: Double, angleDegrees: Double): SkyItem? {
        val radians = Math.toRadians(angleDegrees)
        return model.hitTest(radius * cos(radians), radius * sin(radians))
    }

    @Test
    fun tapOnSolarRingAtNoonSelectsDay() {
        val noon = LocalDateTime(date, LocalTime(12, 0)).toInstant(zone)

        val item = hit(75.0, angle(noon))

        assertIs<SkyItem.Solar>(item)
        assertIs<SolarPhase.Day>(item.span.phase)
    }

    @Test
    fun tapOnMoonRingSelectsMoonSpan() {
        val time = LocalDateTime(date, LocalTime(18, 0)).toInstant(zone)

        val item = hit(35.0, angle(time))

        assertIs<SkyItem.Moon>(item)
        assertTrue(time >= item.span.start && time <= item.span.end)
    }

    @Test
    fun tapOnLightRingSelectsGoldenHour() {
        val golden = day.lightSpans.last { it.lightState == LightState.GoldenHourDusk }
        val middle = golden.start + (golden.end - golden.start) / 2

        val item = hit(62.0, angle(middle))

        assertEquals(SkyItem.Light(golden), item)
    }

    @Test
    fun tapNearSunsetMarkerSelectsEvent() {
        val sunset = day.solarEvents.filterIsInstance<SolarEvent.Sunset>().single()

        val item = hit(89.0, angle(sunset.time) + 2.0)

        assertEquals(SkyItem.SolarEventItem(sunset), item)
    }

    @Test
    fun tapInWedgeOrCentreOrOutsideReturnsNull() {
        assertNull(hit(75.0, 90.0))
        assertNull(hit(10.0, 270.0))
        assertNull(hit(99.0, 270.0))
    }
}
