/*
 * Based on kastro-demo by James Yox (GPL-3.0)
 * https://github.com/jamesyox/kastro-demo
 */

package app.skyward.data.sky

import app.skyward.domain.SolarPhaseSpan
import dev.jamesyox.kastro.sol.SolarEvent
import dev.jamesyox.kastro.sol.SolarPhase
import dev.jamesyox.kastro.sol.calculateSolarState
import kotlin.time.Instant

private data class SolarPhaseElement(
    val start: Instant,
    val startEvent: SolarEvent?,
    val phase: SolarPhase,
)

internal fun List<SolarEvent>.solarPhaseSpans(
    range: OpenEndRange<Instant>,
    location: Pair<Double, Double>,
): List<SolarPhaseSpan> = filter {
    it !is SolarEvent.LightEvent && it !is SolarEvent.HorizonEvent
}
    .scan(
        SolarPhaseElement(
            start = range.start,
            startEvent = null,
            phase = range.start.calculateSolarState(location).solarPhase,
        ),
    ) { current, event ->
        current.after(event)
    }
    .distinctUntilChangedBy { it }
    .plus(
        SolarPhaseElement(
            start = range.endExclusive,
            startEvent = null,
            phase = range.endExclusive.calculateSolarState(location).solarPhase,
        ),
    )
    .zipWithNext { first, second ->
        SolarPhaseSpan(
            start = first.start,
            end = second.start,
            phase = first.phase,
            startEvent = first.startEvent,
            endEvent = second.startEvent,
        )
    }

private fun SolarPhaseElement.after(event: SolarEvent): SolarPhaseElement {
    val next =
        when (event) {
            is SolarEvent.AstronomicalDawn -> SolarPhase.AstronomicalDawn
            is SolarEvent.AstronomicalDusk -> SolarPhase.AstronomicalDusk
            is SolarEvent.CivilDawn -> SolarPhase.CivilDawn
            is SolarEvent.CivilDusk -> SolarPhase.CivilDusk
            is SolarEvent.Day -> SolarPhase.Day
            is SolarEvent.NauticalDawn -> SolarPhase.NauticalDawn
            is SolarEvent.NauticalDusk -> SolarPhase.NauticalDusk
            is SolarEvent.Night -> SolarPhase.Night
            is SolarEvent.Nadir -> phaseAfterNadir()
            is SolarEvent.Noon -> phaseAfterNoon()
            else -> null
        }
    return if (next == null) this else SolarPhaseElement(event.time, event, next)
}

/** At nadir an evening twilight rolls over into the matching morning twilight. */
private fun SolarPhaseElement.phaseAfterNadir(): SolarPhase? =
    when (phase) {
        SolarPhase.AstronomicalDusk -> SolarPhase.AstronomicalDawn
        SolarPhase.CivilDusk -> SolarPhase.CivilDawn
        SolarPhase.NauticalDusk -> SolarPhase.NauticalDawn
        else -> null
    }

/** At noon a morning twilight (polar regions) rolls over into the matching evening twilight. */
private fun SolarPhaseElement.phaseAfterNoon(): SolarPhase? =
    when (phase) {
        SolarPhase.AstronomicalDawn -> SolarPhase.AstronomicalDusk
        SolarPhase.CivilDawn -> SolarPhase.CivilDusk
        SolarPhase.NauticalDawn -> SolarPhase.NauticalDusk
        else -> null
    }
