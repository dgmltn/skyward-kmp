/*
 * Based on kastro-demo by James Yox (GPL-3.0)
 * https://github.com/yoxjames/kastro-demo
 */

package app.skyward.sky.dial

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import app.skyward.dial.DialPalette
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

private val MoonLit = Color.White
private val MoonShadow = DialPalette.MoonDown.toColor()

private const val HALF_TURN = 180f
private const val TOP = -90f
private const val BOTTOM = 90f

/**
 * Draws the moon as seen at [phaseDegrees] (0 new, 90 first quarter, 180 full, 270 last quarter): a
 * lit disc of [diameter] centred on [center], with the dark part painted over it.
 */
internal fun DrawScope.drawMoon(
    phaseDegrees: Double,
    center: Offset,
    diameter: Float,
) {
    val radius = diameter / 2
    drawCircle(color = MoonLit, radius = radius, center = center)
    translate(center.x, center.y) { drawPath(moonShadow(phaseDegrees, radius), MoonShadow) }
}

/**
 * The dark part of the moon, centred on the origin: the semicircle on the unlit side, closed by a
 * half-ellipse whose x-radius is `radius·cos(phase)`, bulging into the lit side for a crescent and
 * back into the dark side for a gibbous moon.
 */
private fun moonShadow(phaseDegrees: Double, radius: Float): Path {
    val isWaning = phaseDegrees > HALF_TURN
    val terminatorX = radius * cos(phaseDegrees * PI / HALF_TURN)
    val isGibbous = terminatorX < 0
    val terminatorRadius = abs(terminatorX).toFloat()
    val disc = Rect(Offset.Zero, radius)
    val terminator = Rect(-terminatorRadius, -radius, terminatorRadius, radius)
    return Path().apply {
        moveTo(0f, -radius)
        arcTo(disc, TOP, if (isWaning) HALF_TURN else -HALF_TURN, forceMoveTo = false)
        if (terminatorRadius > 0f) {
            val sweep = if (isGibbous xor isWaning) HALF_TURN else -HALF_TURN
            arcTo(terminator, BOTTOM, sweep, forceMoveTo = false)
        }
        close()
    }
}
