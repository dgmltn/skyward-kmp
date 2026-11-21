package app.skyward.dial

import app.skyward.domain.SkyItem

data class DialColor(val argb: Long, val luminance: Float)

data class DialArc(
    val radius: Double,
    val width: Double,
    val startDegrees: Double,
    val sweepDegrees: Double,
    val color: DialColor,
    val alpha: Float,
    val item: SkyItem,
)

data class DialTick(
    val angleDegrees: Double,
    val innerRadius: Double,
    val outerRadius: Double,
    val width: Double,
    val color: DialColor,
)

data class DialLabel(
    val angleDegrees: Double,
    val radius: Double,
    val text: String,
    val color: DialColor,
)

enum class MarkerGlyph {
    SUNRISE,
    SUNSET,
    NOON,
    NADIR,
    MOONRISE,
    MOONSET,
    NEW_MOON,
    FIRST_QUARTER,
    FULL_MOON,
    LAST_QUARTER,
}

data class DialMarker(
    val tick: DialTick,
    val glyph: MarkerGlyph,
    val glyphColor: DialColor,
    val item: SkyItem,
)

data class DialHand(val angleDegrees: Double, val length: Double, val color: DialColor)

data class DialMoonIcon(
    val angleDegrees: Double,
    val radius: Double,
    val size: Double,
    val phaseDegrees: Double,
)

data class DialModel(
    val arcs: List<DialArc>,
    val hourTicks: List<DialTick>,
    val hourLabels: List<DialLabel>,
    val markers: List<DialMarker>,
    val selectedEventTick: DialTick?,
    val hand: DialHand?,
    val moonIcon: DialMoonIcon?,
)
