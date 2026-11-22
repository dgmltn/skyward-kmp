/*
 * Based on kastro-demo by James Yox (GPL-3.0)
 * https://github.com/yoxjames/kastro-demo
 */

@file:Suppress("MagicNumber")

package app.skyward.sky.dial

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.skyward.design.SkywardPreview
import app.skyward.dial.DialPalette
import app.skyward.dial.MarkerGlyph
import dev.jamesyox.kastro.luna.LunarEvent

/** Width and height of a marker glyph, in dial units. */
private const val GLYPH_SIZE = 4f

/** The glyph paths below are drawn in a 6x6 box centred on the origin. */
private const val DESIGN_SIZE = 6f
private const val DESIGN_HALF = DESIGN_SIZE / 2

private const val ARROW_WIDTH = 0.8f
private const val NADIR_OUTLINE_WIDTH = 0.2f

private val ArrowColor = Color.Red

private val RiseArrow =
    Path().apply {
        moveTo(0f, 3f)
        lineTo(0f, 1f)
        moveTo(-1f, 2f)
        lineTo(0f, 1f)
        lineTo(1f, 2f)
    }

private val SetArrow =
    Path().apply {
        moveTo(0f, 0.5f)
        lineTo(0f, 2.5f)
        moveTo(-1f, 1.5f)
        lineTo(0f, 2.5f)
        lineTo(1f, 1.5f)
    }

private val UpTriangle =
    Path().apply {
        moveTo(-DESIGN_HALF, DESIGN_HALF)
        lineTo(0f, -DESIGN_HALF)
        lineTo(DESIGN_HALF, DESIGN_HALF)
        close()
    }

private val DownTriangle =
    Path().apply {
        moveTo(-DESIGN_HALF, -DESIGN_HALF)
        lineTo(0f, DESIGN_HALF)
        lineTo(DESIGN_HALF, -DESIGN_HALF)
        close()
    }

/** Draws [glyph] upright, centred on [center], in [color] where the glyph takes one. */
internal fun DrawScope.drawMarkerGlyph(
    glyph: MarkerGlyph,
    color: Color,
    center: Offset,
) {
    when (glyph) {
        MarkerGlyph.NEW_MOON -> drawMoon(LunarEvent.PhaseEvent.NewMoon.phase, center, GLYPH_SIZE)
        MarkerGlyph.FIRST_QUARTER ->
            drawMoon(LunarEvent.PhaseEvent.FirstQuarter.phase, center, GLYPH_SIZE)
        MarkerGlyph.FULL_MOON -> drawMoon(LunarEvent.PhaseEvent.FullMoon.phase, center, GLYPH_SIZE)
        MarkerGlyph.LAST_QUARTER ->
            drawMoon(LunarEvent.PhaseEvent.LastQuarter.phase, center, GLYPH_SIZE)
        MarkerGlyph.SUNRISE,
        MarkerGlyph.MOONRISE -> inDesignBox(center) { drawHorizonGlyph(color, RiseArrow) }
        MarkerGlyph.SUNSET,
        MarkerGlyph.MOONSET -> inDesignBox(center) { drawHorizonGlyph(color, SetArrow) }
        MarkerGlyph.NOON -> inDesignBox(center) { drawPath(UpTriangle, DialPalette.Day.toColor()) }
        MarkerGlyph.NADIR -> inDesignBox(center) { drawNadirGlyph() }
    }
}

private inline fun DrawScope.inDesignBox(
    center: Offset,
    crossinline draw: DrawScope.() -> Unit,
) {
    withTransform(
        {
            translate(center.x, center.y)
            scale(GLYPH_SIZE / DESIGN_SIZE, pivot = Offset.Zero)
        },
    ) {
        draw()
    }
}

/** The sun or moon half above the horizon, with an arrow showing which way it is going. */
private fun DrawScope.drawHorizonGlyph(color: Color, arrow: Path) {
    drawArc(
        color = color,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(-DESIGN_HALF, -DESIGN_HALF),
        size = Size(DESIGN_SIZE, DESIGN_SIZE),
    )
    drawPath(arrow, ArrowColor, style = Stroke(width = ARROW_WIDTH))
}

private fun DrawScope.drawNadirGlyph() {
    drawPath(DownTriangle, DialPalette.Night.toColor())
    drawPath(DownTriangle, Color.White, style = Stroke(width = NADIR_OUTLINE_WIDTH))
}

@Preview
@Composable
private fun Preview_MarkerGlyphs() {
    SkywardPreview {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MarkerGlyph.entries.forEach { glyph ->
                val color =
                    if (glyph == MarkerGlyph.SUNRISE || glyph == MarkerGlyph.SUNSET) {
                        DialPalette.SunGlyph
                    } else {
                        DialPalette.MoonGlyph
                    }
                Canvas(modifier = Modifier.size(24.dp)) {
                    withTransform({ scale(size.minDimension / GLYPH_SIZE, pivot = Offset.Zero) }) {
                        drawMarkerGlyph(glyph, color.toColor(), Offset(GLYPH_SIZE, GLYPH_SIZE) / 2f)
                    }
                }
            }
        }
    }
}
