/*
 * Based on kastro-demo by James Yox (GPL-3.0)
 * https://github.com/yoxjames/kastro-demo
 */

package app.skyward.sky.dial

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.skyward.design.SkywardPreview
import app.skyward.dial.DialArc
import app.skyward.dial.DialGeometry
import app.skyward.dial.DialHand
import app.skyward.dial.DialModel
import app.skyward.dial.DialTick
import app.skyward.dial.buildDialModel
import app.skyward.domain.SkyItem
import dev.jamesyox.kastro.sol.SolarEvent

/** The dial's 200x200 box, in dial units. */
private const val DIAL_SIZE = 200f
private const val LABEL_FONT_SIZE = 3f
private const val HAND_WIDTH = 0.8f
private const val CENTRE_DOT_RADIUS = 3f

/**
 * The sun and moon dial for one day, drawn square and scaled to fit. Taps are reported in dial
 * units: origin at the centre, y down, 100 to an edge.
 */
@Composable
fun DialCanvas(
    model: DialModel,
    onTap: (x: Double, y: Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val currentOnTap by rememberUpdatedState(onTap)
    Spacer(
        modifier =
            modifier
                .aspectRatio(1f)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val scale = size.width.coerceAtMost(size.height) / DIAL_SIZE
                        currentOnTap(
                            ((offset.x - size.width / 2f) / scale).toDouble(),
                            ((offset.y - size.height / 2f) / scale).toDouble(),
                        )
                    }
                }
                .drawWithCache {
                    val scale = size.minDimension / DIAL_SIZE
                    val labelStyle = TextStyle(fontSize = (LABEL_FONT_SIZE * scale).toSp())
                    val labels =
                        model.hourLabels.map { label ->
                            val layout =
                                textMeasurer.measure(
                                    text = label.text,
                                    style = labelStyle.copy(color = label.color.toColor()),
                                )
                            PlacedLabel(layout, polar(label.radius, label.angleDegrees) * scale)
                        }
                    onDrawBehind {
                        withTransform(
                            {
                                translate(center.x, center.y)
                                scale(scale, pivot = Offset.Zero)
                            },
                        ) {
                            drawDial(model)
                        }
                        labels.forEach { (layout, position) ->
                            val size = layout.size
                            drawText(
                                layout,
                                topLeft =
                                    center + position - Offset(size.width / 2f, size.height / 2f),
                            )
                        }
                    }
                },
    )
}

private data class PlacedLabel(val layout: TextLayoutResult, val position: Offset)

/** Draws everything but the hour labels, in dial units with the origin at the centre. */
private fun DrawScope.drawDial(model: DialModel) {
    model.hourTicks.forEach { drawTick(it) }
    model.arcs.forEach { drawRing(it) }
    model.hand?.let { drawHand(it) }
    model.moonIcon?.let {
        drawMoon(it.phaseDegrees, polar(it.radius, it.angleDegrees), it.size.toFloat())
    }
    model.markers.forEach {
        drawTick(it.tick)
        drawMarkerGlyph(
            glyph = it.glyph,
            color = it.glyphColor.toColor(),
            center = polar(DialGeometry.EVENT_GLYPH_RADIUS, it.tick.angleDegrees),
        )
    }
    model.selectedEventTick?.let { drawTick(it) }
}

private fun DrawScope.drawRing(arc: DialArc) {
    val radius = arc.radius.toFloat()
    drawArc(
        color = arc.color.toColor(),
        startAngle = arc.startDegrees.toFloat(),
        sweepAngle = arc.sweepDegrees.toFloat(),
        useCenter = false,
        topLeft = Offset(-radius, -radius),
        size = Size(2 * radius, 2 * radius),
        alpha = arc.alpha,
        style = Stroke(width = arc.width.toFloat(), cap = StrokeCap.Butt),
    )
}

private fun DrawScope.drawTick(tick: DialTick) {
    drawLine(
        color = tick.color.toColor(),
        start = polar(tick.innerRadius, tick.angleDegrees),
        end = polar(tick.outerRadius, tick.angleDegrees),
        strokeWidth = tick.width.toFloat(),
    )
}

private fun DrawScope.drawHand(hand: DialHand) {
    val color = hand.color.toColor()
    drawLine(
        color = color,
        start = Offset.Zero,
        end = polar(hand.length, hand.angleDegrees),
        strokeWidth = HAND_WIDTH,
    )
    drawCircle(color = color, radius = CENTRE_DOT_RADIUS, center = Offset.Zero)
}

@Preview
@Composable
private fun Preview_DialCanvas_London() {
    SkywardPreview {
        DialCanvas(
            model =
                buildDialModel(
                    day = DialPreviewFixtures.London,
                    now = DialPreviewFixtures.LondonNow,
                    selected = null,
                    use24Hour = false,
                ),
            onTap = { _, _ -> },
            modifier = Modifier.size(360.dp),
        )
    }
}

@Preview
@Composable
private fun Preview_DialCanvas_London_SelectedSpan() {
    val day = DialPreviewFixtures.London
    SkywardPreview {
        DialCanvas(
            model =
                buildDialModel(
                    day = day,
                    now = DialPreviewFixtures.LondonNow,
                    selected = SkyItem.Light(day.lightSpans[1]),
                    use24Hour = false,
                ),
            onTap = { _, _ -> },
            modifier = Modifier.size(360.dp),
        )
    }
}

@Preview
@Composable
private fun Preview_DialCanvas_London_SelectedEvent() {
    val day = DialPreviewFixtures.London
    val sunset = day.solarEvents.first { it is SolarEvent.Sunset }
    SkywardPreview {
        DialCanvas(
            model =
                buildDialModel(
                    day = day,
                    now = DialPreviewFixtures.LondonNow,
                    selected = SkyItem.SolarEventItem(sunset),
                    use24Hour = false,
                ),
            onTap = { _, _ -> },
            modifier = Modifier.size(360.dp),
        )
    }
}

@Preview
@Composable
private fun Preview_DialCanvas_TromsoPolarDay() {
    SkywardPreview {
        DialCanvas(
            model =
                buildDialModel(
                    day = DialPreviewFixtures.Tromso,
                    now = DialPreviewFixtures.TromsoNow,
                    selected = null,
                    use24Hour = true,
                ),
            onTap = { _, _ -> },
            modifier = Modifier.size(360.dp),
        )
    }
}
