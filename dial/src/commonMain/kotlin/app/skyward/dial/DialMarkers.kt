/*
 * Based on kastro-demo by James Yox (GPL-3.0)
 * https://github.com/jamesyox/kastro-demo
 */

package app.skyward.dial

import app.skyward.domain.SkyDay
import app.skyward.domain.SkyItem
import dev.jamesyox.kastro.luna.LunarEvent
import dev.jamesyox.kastro.sol.SolarEvent
import kotlin.time.Instant

private const val EVENT_TICK_WIDTH = 0.3
private const val SELECTED_EVENT_TICK_WIDTH = 0.5

private fun marker(
    angle: Double,
    glyph: MarkerGlyph,
    glyphColor: DialColor,
    item: SkyItem,
) =
    DialMarker(
        tick =
            DialTick(
                angleDegrees = angle,
                innerRadius = DialGeometry.EVENT_INNER,
                outerRadius = DialGeometry.EVENT_OUTER,
                width = EVENT_TICK_WIDTH,
                color = DialPalette.EventTick,
            ),
        glyph = glyph,
        glyphColor = glyphColor,
        item = item,
    )

internal fun selectedEventTick(angle: Double) =
    DialTick(
        angleDegrees = angle,
        innerRadius = DialGeometry.SELECTED_EVENT_INNER,
        outerRadius = DialGeometry.EVENT_OUTER,
        width = SELECTED_EVENT_TICK_WIDTH,
        color = DialPalette.Hand,
    )

internal fun SkyItem.isEvent() = eventTime() != null

internal fun SkyItem.eventTime(): Instant? =
    when (this) {
        is SkyItem.SolarEventItem -> event.time
        is SkyItem.LunarEventItem -> event.time
        is SkyItem.Solar,
        is SkyItem.Light,
        is SkyItem.Moon -> null
    }

/** Only these solar events are drawn on the dial; the rest are covered by the arcs. */
private fun SolarEvent.glyph(): Pair<MarkerGlyph, DialColor>? =
    when (this) {
        is SolarEvent.Sunrise -> MarkerGlyph.SUNRISE to DialPalette.SunGlyph
        is SolarEvent.Sunset -> MarkerGlyph.SUNSET to DialPalette.SunGlyph
        is SolarEvent.Noon -> MarkerGlyph.NOON to DialPalette.SunGlyph
        is SolarEvent.Nadir -> MarkerGlyph.NADIR to DialPalette.SunGlyph
        else -> null
    }

private fun LunarEvent.glyph(): Pair<MarkerGlyph, DialColor> {
    val glyph =
        when (this) {
            is LunarEvent.HorizonEvent.Moonrise -> MarkerGlyph.MOONRISE
            is LunarEvent.HorizonEvent.Moonset -> MarkerGlyph.MOONSET
            is LunarEvent.PhaseEvent.NewMoon -> MarkerGlyph.NEW_MOON
            is LunarEvent.PhaseEvent.FirstQuarter -> MarkerGlyph.FIRST_QUARTER
            is LunarEvent.PhaseEvent.FullMoon -> MarkerGlyph.FULL_MOON
            is LunarEvent.PhaseEvent.LastQuarter -> MarkerGlyph.LAST_QUARTER
        }
    return glyph to DialPalette.MoonGlyph
}

internal fun SkyDay.markers(angle: (Instant) -> Double): List<DialMarker> {
    val solar = solarEvents.mapNotNull { event ->
        event.glyph()?.let { (glyph, color) ->
            marker(angle(event.time), glyph, color, SkyItem.SolarEventItem(event))
        }
    }
    val lunar = lunarEvents.map { event ->
        val (glyph, color) = event.glyph()
        marker(angle(event.time), glyph, color, SkyItem.LunarEventItem(event))
    }
    return (solar + lunar).sortedBy { it.tick.angleDegrees }
}
