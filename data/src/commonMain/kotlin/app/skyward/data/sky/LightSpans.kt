/*
 * Based on kastro-demo by James Yox (GPL-3.0)
 * https://github.com/yoxjames/kastro-demo
 */

package app.skyward.data.sky

import app.skyward.domain.LightSpan
import dev.jamesyox.kastro.sol.LightPhase
import dev.jamesyox.kastro.sol.LightState
import dev.jamesyox.kastro.sol.SolarEvent
import dev.jamesyox.kastro.sol.calculateSolarState
import kotlin.time.Instant

private data class LightStateElement(
    val start: Instant,
    val startEvent: SolarEvent?,
    val lightStates: List<LightState>,
)

internal fun List<SolarEvent>.lightSpans(
    range: OpenEndRange<Instant>,
    location: Pair<Double, Double>,
): List<LightSpan> {
    val states =
        scan(
            LightStateElement(
                start = range.start,
                startEvent = null,
                lightStates = range.start.calculateSolarState(location).lightStates,
            ),
        ) { current, event ->
            LightStateElement(
                start = event.time,
                startEvent = event,
                lightStates = lightStatesAfter(current.lightStates, event),
            )
        }

    return states.spansOf<LightPhase.GoldenHour>(range) + states.spansOf<LightPhase.BlueHour>(range)
}

private inline fun <reified P : LightPhase> List<LightStateElement>.spansOf(
    range: OpenEndRange<Instant>,
): List<LightSpan> {
    val boundaries = map {
        it.copy(lightStates = it.lightStates.filter { state -> state is P })
    }
        .distinctUntilChangedBy { it.lightStates }
    val closing =
        LightStateElement(start = range.endExclusive, startEvent = null, lightStates = emptyList())
    return (boundaries + closing).zipWithNext().mapNotNull { (first, second) ->
        first.lightStates.firstOrNull()?.let { lightState ->
            LightSpan(
                start = first.start,
                end = second.start,
                lightState = lightState,
                startEvent = first.startEvent,
                endEvent = second.startEvent,
            )
        }
    }
}

private fun lightStatesAfter(current: List<LightState>, event: SolarEvent): List<LightState> =
    when (event) {
        is SolarEvent.Noon -> current.map(::eveningOf)
        is SolarEvent.Nadir -> current.map(::morningOf)
        else -> event.lightStateStarted()?.let { current + it } ?: current - event.lightStateEnded()
    }

private fun SolarEvent.lightStateStarted(): LightState? =
    when (this) {
        is SolarEvent.BlueHourDawn -> LightState.BlueHourDawn
        is SolarEvent.BlueHourDusk -> LightState.BlueHourDusk
        is SolarEvent.GoldenHourDawn -> LightState.GoldenHourDawn
        is SolarEvent.GoldenHourDusk -> LightState.GoldenHourDusk
        else -> null
    }

private fun SolarEvent.lightStateEnded(): Set<LightState> =
    when (this) {
        is SolarEvent.BlueHourDawnEnd -> setOf(LightState.BlueHourDawn)
        is SolarEvent.BlueHourDuskEnd -> setOf(LightState.BlueHourDusk)
        is SolarEvent.GoldenHourDawnEnd -> setOf(LightState.GoldenHourDawn)
        is SolarEvent.GoldenHourDuskEnd -> setOf(LightState.GoldenHourDusk)
        else -> emptySet()
    }

private fun eveningOf(state: LightState): LightState =
    when (state) {
        LightState.GoldenHourDawn -> LightState.GoldenHourDusk
        LightState.BlueHourDawn -> LightState.BlueHourDusk
        else -> state
    }

private fun morningOf(state: LightState): LightState =
    when (state) {
        LightState.GoldenHourDusk -> LightState.GoldenHourDawn
        LightState.BlueHourDusk -> LightState.BlueHourDawn
        else -> state
    }
