package app.skyward.dial

import app.skyward.domain.SkyItem
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot

private const val FULL_CIRCLE = 360.0
private const val GAP_START_DEGREES = DialGeometry.WEDGE_DEGREES / 2 + 90
private const val DEGREES_PER_RADIAN = 180.0 / PI
private const val MARKER_MAX_DEGREES = 3.0

@Suppress("MagicNumber")
private object Bands {
    val Marker = 83.0..94.0
    val Solar = 65.0..85.0
    val Light = 57.0..64.999
    val Moon = 25.0..45.0
}

/**
 * Returns the item under a tap at [x], [y] in dial units, or null for the centre, wedge and
 * outside.
 */
fun DialModel.hitTest(x: Double, y: Double): SkyItem? {
    val radius = hypot(x, y)
    val angle = dialAngle(atan2(y, x) * DEGREES_PER_RADIAN) ?: return null
    return when (radius) {
        in Bands.Marker -> nearestMarker(angle) ?: arcAt(radius, angle)
        else -> arcAt(radius, angle)
    }
}

/** Normalises to the dial's 100..440 degree range, or null inside the bottom wedge. */
private fun dialAngle(degrees: Double): Double? {
    val offset = ((degrees - GAP_START_DEGREES) % FULL_CIRCLE + FULL_CIRCLE) % FULL_CIRCLE
    return if (offset > FULL_CIRCLE - DialGeometry.WEDGE_DEGREES) null
    else offset + GAP_START_DEGREES
}

private fun DialModel.nearestMarker(angle: Double): SkyItem? =
    markers
        .minByOrNull { abs(it.tick.angleDegrees - angle) }
        ?.takeIf { abs(it.tick.angleDegrees - angle) <= MARKER_MAX_DEGREES }
        ?.item

private fun DialModel.arcAt(radius: Double, angle: Double): SkyItem? {
    val band =
        when (radius) {
            in Bands.Solar -> Bands.Solar
            in Bands.Light -> Bands.Light
            in Bands.Moon -> Bands.Moon
            else -> return null
        }
    return arcs
        .firstOrNull {
            it.radius in band &&
                angle >= it.startDegrees &&
                angle <= it.startDegrees + it.sweepDegrees
        }
        ?.item
}
