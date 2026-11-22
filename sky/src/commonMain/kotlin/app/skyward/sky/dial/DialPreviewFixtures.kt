@file:Suppress("MagicNumber")

package app.skyward.sky.dial

import app.skyward.domain.LightSpan
import app.skyward.domain.MoonSpan
import app.skyward.domain.SkyDay
import app.skyward.domain.SkyNow
import app.skyward.domain.SolarPhaseSpan
import dev.jamesyox.kastro.common.HorizonState
import dev.jamesyox.kastro.luna.LunarEvent
import dev.jamesyox.kastro.luna.LunarPhase
import dev.jamesyox.kastro.sol.LightState
import dev.jamesyox.kastro.sol.SolarEvent
import dev.jamesyox.kastro.sol.SolarPhase
import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant

/**
 * Days and moments for dial previews, copied from what `KastroSkyRepository` computes for the June
 * 2026 solstice. Span boundary events are left out because the dial never reads them.
 */
internal object DialPreviewFixtures {
    private val Solstice = LocalDate(2026, 6, 21)

    val London: SkyDay =
        PreviewDay(TimeZone.of("Europe/London")).build {
            solar(t("00:00:00"), t("01:02:15"), SolarPhase.AstronomicalDusk)
            solar(t("01:02:15"), t("02:40:56"), SolarPhase.AstronomicalDawn)
            solar(t("02:40:56"), t("03:55:26"), SolarPhase.NauticalDawn)
            solar(t("03:55:26"), t("04:50:18"), SolarPhase.CivilDawn)
            solar(t("04:50:18"), t("21:14:41"), SolarPhase.Day)
            solar(t("21:14:41"), t("22:09:28"), SolarPhase.CivilDusk)
            solar(t("22:09:28"), t("23:24:20"), SolarPhase.NauticalDusk)
            solar(t("23:24:20"), end, SolarPhase.AstronomicalDusk)
            light(t("03:55:26"), t("05:37:34"), LightState.GoldenHourDawn)
            light(t("20:27:29"), t("22:09:28"), LightState.GoldenHourDusk)
            light(t("03:34:14"), t("04:15:00"), LightState.BlueHourDawn)
            light(t("21:50:00"), t("22:31:01"), LightState.BlueHourDusk)
            moon(t("00:00:00"), t("00:34:40"), HorizonState.Up)
            moon(t("00:34:40"), t("12:40:33"), HorizonState.Down)
            moon(t("12:40:33"), end, HorizonState.Up)
            solarEvents +=
                listOf(
                    SolarEvent.Nadir(t("01:02:15")),
                    SolarEvent.NauticalDawn(t("02:40:56")),
                    SolarEvent.BlueHourDawn(t("03:34:14")),
                    SolarEvent.CivilDawn(t("03:55:26")),
                    SolarEvent.GoldenHourDawn(t("03:55:26")),
                    SolarEvent.BlueHourDawnEnd(t("04:15:00")),
                    SolarEvent.Sunrise(t("04:43:19")),
                    SolarEvent.SunriseEnd(t("04:47:43")),
                    SolarEvent.Day(t("04:50:18")),
                    SolarEvent.GoldenHourDawnEnd(t("05:37:34")),
                    SolarEvent.Noon(t("13:02:21")),
                    SolarEvent.GoldenHourDusk(t("20:27:29")),
                    SolarEvent.CivilDusk(t("21:14:41")),
                    SolarEvent.SunsetBegin(t("21:17:18")),
                    SolarEvent.Sunset(t("21:21:46")),
                    SolarEvent.BlueHourDusk(t("21:50:00")),
                    SolarEvent.NauticalDusk(t("22:09:28")),
                    SolarEvent.GoldenHourDuskEnd(t("22:09:28")),
                    SolarEvent.BlueHourDuskEnd(t("22:31:01")),
                    SolarEvent.AstronomicalDusk(t("23:24:20")),
                )
            lunarEvents +=
                listOf(
                    LunarEvent.HorizonEvent.Moonset(t("00:34:40")),
                    LunarEvent.HorizonEvent.Moonrise(t("12:40:33")),
                    LunarEvent.PhaseEvent.FirstQuarter(t("22:56:50")),
                )
        }

    /** London at 3pm. */
    val LondonNow =
        SkyNow(
            instant = Instant.parse("2026-06-21T14:00:00Z"),
            solarPhase = SolarPhase.Day,
            sunAltitude = 53.99,
            sunAzimuth = 229.99,
            moonAltitude = 19.98,
            moonAzimuth = 118.01,
            moonIlluminatedFraction = 0.466,
            moonPhaseDegrees = 86.06,
            moonPhase = LunarPhase.Intermediate.WaxingCrescent,
            nextSunrise = Instant.parse("2026-06-22T03:43:33Z"),
            nextSunset = Instant.parse("2026-06-21T20:21:47Z"),
            nextMoonrise = Instant.parse("2026-06-22T12:55:43Z"),
            nextMoonset = Instant.parse("2026-06-21T23:46:04Z"),
            nextFullMoon = Instant.parse("2026-06-29T23:57:00Z"),
            nextNewMoon = Instant.parse("2026-07-14T09:44:00Z"),
        )

    val Tromso: SkyDay =
        PreviewDay(TimeZone.of("Europe/Oslo")).build {
            solar(t("00:00:00"), end, SolarPhase.Day)
            light(t("00:00:00"), t("00:45:54"), LightState.GoldenHourDusk)
            light(t("00:45:54"), t("02:56:58"), LightState.GoldenHourDawn)
            light(t("22:35:22"), end, LightState.GoldenHourDusk)
            moon(t("00:00:00"), t("00:37:29"), HorizonState.Up)
            moon(t("00:37:29"), t("12:17:51"), HorizonState.Down)
            moon(t("12:17:51"), end, HorizonState.Up)
            solarEvents +=
                listOf(
                    SolarEvent.Nadir(t("00:45:54")),
                    SolarEvent.GoldenHourDawnEnd(t("02:56:58")),
                    SolarEvent.Noon(t("12:46:01")),
                    SolarEvent.GoldenHourDusk(t("22:35:22")),
                )
            lunarEvents +=
                listOf(
                    LunarEvent.HorizonEvent.Moonset(t("00:37:29")),
                    LunarEvent.HorizonEvent.Moonrise(t("12:17:51")),
                    LunarEvent.PhaseEvent.FirstQuarter(t("23:56:50")),
                )
        }

    /** Tromsø at 3pm, with the sun up all day and all night. */
    val TromsoNow =
        SkyNow(
            instant = Instant.parse("2026-06-21T13:00:00Z"),
            solarPhase = SolarPhase.Day,
            sunAltitude = 39.74,
            sunAzimuth = 221.16,
            moonAltitude = 12.36,
            moonAzimuth = 126.69,
            moonIlluminatedFraction = 0.461,
            moonPhaseDegrees = 85.56,
            moonPhase = LunarPhase.Intermediate.WaxingCrescent,
            nextSunrise = Instant.parse("2026-07-25T23:05:10Z"),
            nextSunset = Instant.parse("2026-07-25T22:38:03Z"),
            nextMoonrise = Instant.parse("2026-06-22T12:13:12Z"),
            nextMoonset = Instant.parse("2026-06-21T22:13:51Z"),
            nextFullMoon = Instant.parse("2026-06-29T23:57:00Z"),
            nextNewMoon = Instant.parse("2026-07-14T09:44:00Z"),
        )

    private class PreviewDay(private val timeZone: TimeZone) {
        val end = Solstice.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)
        private val solarPhases = mutableListOf<SolarPhaseSpan>()
        private val lightSpans = mutableListOf<LightSpan>()
        private val moonSpans = mutableListOf<MoonSpan>()
        val solarEvents = mutableListOf<SolarEvent>()
        val lunarEvents = mutableListOf<LunarEvent>()

        fun t(time: String): Instant = Solstice.atTime(LocalTime.parse(time)).toInstant(timeZone)

        fun solar(start: Instant, end: Instant, phase: SolarPhase) {
            solarPhases += SolarPhaseSpan(start, end, phase, startEvent = null, endEvent = null)
        }

        fun light(start: Instant, end: Instant, state: LightState) {
            lightSpans += LightSpan(start, end, state, startEvent = null, endEvent = null)
        }

        fun moon(start: Instant, end: Instant, state: HorizonState) {
            moonSpans += MoonSpan(start, end, state, startEvent = null, endEvent = null)
        }

        fun build(content: PreviewDay.() -> Unit): SkyDay {
            content()
            return SkyDay(
                date = Solstice,
                timeZone = timeZone,
                start = Solstice.atStartOfDayIn(timeZone),
                end = end,
                solarPhases = solarPhases,
                lightSpans = lightSpans,
                moonSpans = moonSpans,
                solarEvents = solarEvents,
                lunarEvents = lunarEvents,
            )
        }
    }
}
