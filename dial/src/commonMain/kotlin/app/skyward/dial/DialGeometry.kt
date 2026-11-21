/*
 * Based on kastro-demo by James Yox (GPL-3.0)
 * https://github.com/jamesyox/kastro-demo
 */

package app.skyward.dial

import kotlin.time.Instant

/**
 * Dial units: a 200x200 box centred on the origin, y down. Angles are degrees, 0 = +x, increasing
 * clockwise on screen, matching Compose `drawArc`.
 */
object DialGeometry {
    const val WEDGE_DEGREES = 20.0
    const val SOLAR_RADIUS = 75.0
    const val SOLAR_WIDTH = 20.0
    const val LIGHT_RADIUS = 62.0
    const val LIGHT_WIDTH = 3.0
    const val MOON_RADIUS = 35.0
    const val MOON_WIDTH = 20.0
    const val HOUR_TICK_RADIUS = 88.0
    const val HOUR_LABEL_RADIUS = 95.0
    const val EVENT_INNER = 85.0
    const val EVENT_OUTER = 86.5
    const val EVENT_GLYPH_RADIUS = 89.5
    const val HAND_LENGTH = 91.0
    const val MOON_ICON_RADIUS = 51.0
    const val MOON_ICON_SIZE = 6.0
    const val SELECTED_EVENT_INNER = 25.0

    private const val FULL_CIRCLE = 360.0
    private const val SWEEP_DEGREES = FULL_CIRCLE - WEDGE_DEGREES
    private const val START_DEGREES = WEDGE_DEGREES / 2 + 90

    fun angleOf(instant: Instant, start: Instant, end: Instant): Double {
        val fraction = (instant - start) / (end - start)
        return fraction * SWEEP_DEGREES + START_DEGREES
    }

    /** Returns null for angles inside the gap at the bottom of the dial. */
    fun instantAt(angleDegrees: Double, start: Instant, end: Instant): Instant? {
        val offset = ((angleDegrees - START_DEGREES) % FULL_CIRCLE + FULL_CIRCLE) % FULL_CIRCLE
        if (offset > SWEEP_DEGREES) return null
        return start + (end - start) * (offset / SWEEP_DEGREES)
    }
}
