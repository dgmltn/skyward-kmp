/*
 * Based on kastro-demo by James Yox (GPL-3.0)
 * https://github.com/jamesyox/kastro-demo
 */

package app.skyward.sky.tabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.skyward.design.SkywardPreview
import app.skyward.dial.DialPalette
import app.skyward.domain.SkyNow
import app.skyward.sky.dial.DialPreviewFixtures
import app.skyward.sky.dial.drawMoon
import app.skyward.sky.dial.toColor
import app.skyward.sky.formatOneDecimal
import app.skyward.sky.formatTimeFrom
import app.skyward.sky.title
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private val IconTileSize = 72.dp
private val IconPadding = 12.dp

/** The sun and moon right now: where they are, and when they next rise and set. */
@Composable
fun CurrentTab(
    now: SkyNow,
    timeZone: TimeZone,
    use24Hour: Boolean,
    modifier: Modifier = Modifier,
) {
    val today = now.instant.toLocalDateTime(timeZone).date
    fun next(instant: Instant?) =
        instant?.let { formatTimeFrom(today, it, timeZone, use24Hour) } ?: "None soon"

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BodyCard(
            title = "Sun",
            countdown = countdown("Sun", now.instant, now.nextSunrise, now.nextSunset),
            icon = { SunIcon(Modifier.fillMaxSize()) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Reading("Solar phase", now.solarPhase.title)
            Reading("Altitude", degrees(now.sunAltitude))
            Reading("Azimuth", degrees(now.sunAzimuth))
            Reading("Next sunrise", next(now.nextSunrise))
            Reading("Next sunset", next(now.nextSunset))
        }
        BodyCard(
            title = "Moon",
            countdown = countdown("Moon", now.instant, now.nextMoonrise, now.nextMoonset),
            icon = { MoonIcon(now.moonPhaseDegrees, Modifier.fillMaxSize()) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Reading("Phase", now.moonPhase.title)
            Reading("Illumination", "${formatOneDecimal(now.moonIlluminatedFraction * PERCENT)}%")
            Reading("Altitude", degrees(now.moonAltitude))
            Reading("Azimuth", degrees(now.moonAzimuth))
            Reading("Next moonrise", next(now.nextMoonrise))
            Reading("Next moonset", next(now.nextMoonset))
        }
    }
}

@Composable
private fun BodyCard(
    title: String,
    countdown: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier.size(IconTileSize)
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .padding(IconPadding),
                ) {
                    icon()
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(text = countdown, style = MaterialTheme.typography.titleMedium)
                }
            }
            content()
        }
    }
}

@Composable
private fun SunIcon(modifier: Modifier = Modifier) {
    val color = DialPalette.SunGlyph.toColor()
    Canvas(modifier) { drawCircle(color) }
}

@Composable
private fun MoonIcon(phaseDegrees: Double, modifier: Modifier = Modifier) {
    Canvas(modifier) { drawMoon(phaseDegrees, center, size.minDimension) }
}

@Composable
private fun Reading(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
        )
    }
}

private fun degrees(value: Double) = "${formatOneDecimal(value)}°"

private const val PERCENT = 100

@Preview
@Composable
private fun Preview_CurrentTab_London() {
    SkywardPreview {
        CurrentTab(
            now = DialPreviewFixtures.LondonNow,
            timeZone = DialPreviewFixtures.London.timeZone,
            use24Hour = false,
        )
    }
}

@Preview
@Composable
private fun Preview_CurrentTab_TromsoPolarDay() {
    SkywardPreview {
        CurrentTab(
            now = DialPreviewFixtures.TromsoNow,
            timeZone = DialPreviewFixtures.Tromso.timeZone,
            use24Hour = true,
        )
    }
}
