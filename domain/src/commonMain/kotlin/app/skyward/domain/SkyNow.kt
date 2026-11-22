package app.skyward.domain

import dev.jamesyox.kastro.luna.LunarPhase
import dev.jamesyox.kastro.sol.SolarPhase
import kotlin.time.Instant

data class SkyNow(
    val instant: Instant,
    val solarPhase: SolarPhase,
    val sunAltitude: Double,
    val sunAzimuth: Double,
    val moonAltitude: Double,
    val moonAzimuth: Double,
    val moonIlluminatedFraction: Double,
    val moonPhaseDegrees: Double,
    val moonPhase: LunarPhase.Intermediate,
    val nextSunrise: Instant?,
    val nextSunset: Instant?,
    val nextMoonrise: Instant?,
    val nextMoonset: Instant?,
    val nextFullMoon: Instant,
    val nextNewMoon: Instant,
)
