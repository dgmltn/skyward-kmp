package app.skyward.domain

import dev.jamesyox.kastro.common.HorizonState
import dev.jamesyox.kastro.luna.LunarEvent
import dev.jamesyox.kastro.sol.LightState
import dev.jamesyox.kastro.sol.SolarEvent
import dev.jamesyox.kastro.sol.SolarPhase
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

data class SolarPhaseSpan(
    val start: Instant,
    val end: Instant,
    val phase: SolarPhase,
    val startEvent: SolarEvent?,
    val endEvent: SolarEvent?,
)

data class LightSpan(
    val start: Instant,
    val end: Instant,
    val lightState: LightState,
    val startEvent: SolarEvent?,
    val endEvent: SolarEvent?,
)

data class MoonSpan(
    val start: Instant,
    val end: Instant,
    val horizonState: HorizonState,
    val startEvent: LunarEvent.HorizonEvent?,
    val endEvent: LunarEvent.HorizonEvent?,
)

data class SkyDay(
    val date: LocalDate,
    val timeZone: TimeZone,
    val start: Instant,
    val end: Instant,
    val solarPhases: List<SolarPhaseSpan>,
    val lightSpans: List<LightSpan>,
    val moonSpans: List<MoonSpan>,
    val solarEvents: List<SolarEvent>,
    val lunarEvents: List<LunarEvent>,
)
