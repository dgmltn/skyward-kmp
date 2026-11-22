/*
 * Based on kastro-demo by James Yox (GPL-3.0)
 * https://github.com/yoxjames/kastro-demo
 */

package app.skyward.data.sky

import app.skyward.domain.MoonSpan
import dev.jamesyox.kastro.common.HorizonState
import dev.jamesyox.kastro.luna.LunarEvent
import dev.jamesyox.kastro.luna.calculateLunarState
import kotlin.time.Instant

private data class MoonHorizonElement(
    val start: Instant,
    val startEvent: LunarEvent.HorizonEvent?,
    val horizonState: HorizonState,
)

internal fun List<LunarEvent>.moonSpans(
    range: OpenEndRange<Instant>,
    location: Pair<Double, Double>,
): List<MoonSpan> =
    filterIsInstance<LunarEvent.HorizonEvent>()
        .scan(
            MoonHorizonElement(
                start = range.start,
                startEvent = null,
                horizonState = range.start.calculateLunarState(location).horizonState,
            ),
        ) { _, event ->
            MoonHorizonElement(
                start = event.time,
                startEvent = event,
                horizonState = event.horizonState,
            )
        }
        .plus(
            MoonHorizonElement(
                start = range.endExclusive,
                startEvent = null,
                horizonState = range.endExclusive.calculateLunarState(location).horizonState,
            ),
        )
        .zipWithNext { first, second ->
            MoonSpan(
                start = first.start,
                end = second.start,
                horizonState = first.horizonState,
                startEvent = first.startEvent,
                endEvent = second.startEvent,
            )
        }

private val LunarEvent.HorizonEvent.horizonState: HorizonState
    get() =
        when (this) {
            is LunarEvent.HorizonEvent.Moonrise -> HorizonState.Up
            is LunarEvent.HorizonEvent.Moonset -> HorizonState.Down
        }
