package app.skyward.sky.dial

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import app.skyward.dial.DialColor
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val RADIANS_PER_DEGREE = PI / 180.0

/** The point [radius] dial units from the centre at [angleDegrees], clockwise from +x. */
internal fun polar(radius: Double, angleDegrees: Double): Offset {
    val radians = angleDegrees * RADIANS_PER_DEGREE
    return Offset((radius * cos(radians)).toFloat(), (radius * sin(radians)).toFloat())
}

internal fun DialColor.toColor() = Color(argb)
