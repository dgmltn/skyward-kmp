/*
 * Based on kastro-demo by James Yox (GPL-3.0)
 * https://github.com/jamesyox/kastro-demo
 */

package app.skyward.data.sky

import app.skyward.domain.GeoPoint
import app.skyward.domain.SkyDay
import app.skyward.domain.SkyNow
import app.skyward.domain.SkyRepository
import dev.jamesyox.kastro.luna.LunarEvent
import dev.jamesyox.kastro.luna.LunarEventSequence
import dev.jamesyox.kastro.luna.LunarHorizonEventSequence
import dev.jamesyox.kastro.luna.LunarPhase
import dev.jamesyox.kastro.luna.LunarPhaseSequence
import dev.jamesyox.kastro.luna.calculateLunarState
import dev.jamesyox.kastro.sol.SolarEvent
import dev.jamesyox.kastro.sol.SolarEventSequence
import dev.jamesyox.kastro.sol.SolarEventType
import dev.jamesyox.kastro.sol.calculateSolarState
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus

class KastroSkyRepository : SkyRepository {
    override fun day(point: GeoPoint, date: LocalDate, timeZone: TimeZone): SkyDay {
        val start = date.atStartOfDayIn(timeZone)
        val end = date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)
        val range = start..<end
        val location = point.asPair()

        val solarEvents =
            SolarEventSequence(
                    start = start,
                    location = location,
                    limit = end - start,
                    requestedSolarEvents = SolarEventType.all,
                    height = SEA_LEVEL,
                )
                .toList()
        val lunarEvents =
            LunarEventSequence(
                    start = start,
                    location = location,
                    limit = end - start,
                    requestedLunarEvents = LunarEvent.all,
                )
                .toList()

        return SkyDay(
            date = date,
            timeZone = timeZone,
            start = start,
            end = end,
            solarPhases = solarEvents.solarPhaseSpans(range, location),
            lightSpans = solarEvents.lightSpans(range, location),
            moonSpans = lunarEvents.moonSpans(range, location),
            solarEvents = solarEvents,
            lunarEvents = lunarEvents,
        )
    }

    override fun now(point: GeoPoint, instant: Instant): SkyNow {
        val location = point.asPair()
        val solar = instant.calculateSolarState(location)
        val lunar = instant.calculateLunarState(location)

        return SkyNow(
            instant = instant,
            solarPhase = solar.solarPhase,
            sunAltitude = solar.altitude,
            sunAzimuth = solar.azimuth,
            moonAltitude = lunar.position.altitude,
            moonAzimuth = lunar.position.azimuth,
            moonIlluminatedFraction = lunar.illumination.fraction,
            moonPhaseDegrees = lunar.illumination.phase,
            moonPhase = lunar.phase,
            nextSunrise = nextSolar(location, instant, SolarEvent.Sunrise),
            nextSunset = nextSolar(location, instant, SolarEvent.Sunset),
            nextMoonrise = nextLunar(location, instant, LunarEvent.HorizonEvent.Moonrise),
            nextMoonset = nextLunar(location, instant, LunarEvent.HorizonEvent.Moonset),
            nextFullMoon = nextPhase(instant, LunarEvent.PhaseEvent.FullMoon),
            nextNewMoon = nextPhase(instant, LunarEvent.PhaseEvent.NewMoon),
        )
    }

    private fun nextSolar(
        location: Pair<Double, Double>,
        from: Instant,
        type: SolarEventType,
    ): Instant? =
        SolarEventSequence(
                start = from,
                location = location,
                limit = SEARCH_LIMIT,
                requestedSolarEvents = listOf(type),
                height = SEA_LEVEL,
            )
            .firstOrNull()
            ?.time

    private fun nextLunar(
        location: Pair<Double, Double>,
        from: Instant,
        type: LunarEvent.HorizonEvent.HorizonEventType,
    ): Instant? =
        LunarHorizonEventSequence(
                start = from,
                location = location,
                limit = SEARCH_LIMIT,
                requestedHorizonEvents = listOf(type),
            )
            .firstOrNull()
            ?.time

    /** A lunar month is under 30 days, so there's always one within the search. */
    private fun nextPhase(from: Instant, phase: LunarPhase.Primary): Instant =
        LunarPhaseSequence(
                start = from,
                limit = SEARCH_LIMIT,
                requestedLunarPhases = listOf(phase),
            )
            .first()
            .time

    private fun GeoPoint.asPair(): Pair<Double, Double> = latitude to longitude

    private companion object {
        const val SEA_LEVEL = 0.0
        val SEARCH_LIMIT = 366.days
    }
}
