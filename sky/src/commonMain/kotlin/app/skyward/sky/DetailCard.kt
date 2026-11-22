/*
 * Based on kastro-demo by James Yox (GPL-3.0)
 * https://github.com/yoxjames/kastro-demo
 */

package app.skyward.sky

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.skyward.design.SkywardIcon
import app.skyward.design.SkywardPreview
import app.skyward.dial.DialPalette
import app.skyward.domain.SkyItem
import app.skyward.sky.dial.DialPreviewFixtures
import app.skyward.sky.dial.toColor

/**
 * The selected span or event: its name, when it happens, how long it lasts and what it is. [swatch]
 * is the span's ring colour, or [Color.Unspecified] for an event.
 */
@Composable
fun DetailCard(
    title: String,
    timing: String,
    duration: String?,
    description: String,
    swatch: Color,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (swatch.isSpecified) {
                    Box(
                        modifier =
                            Modifier.padding(end = 8.dp)
                                .size(12.dp)
                                .background(swatch, CircleShape),
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDismiss) { SkywardIcon.Close(contentDescription = "Close") }
            }
            Text(
                text = if (duration == null) timing else "$timing  ·  $duration",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(end = 16.dp),
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, end = 16.dp),
            )
        }
    }
}

@Preview
@Composable
private fun Preview_DetailCard_Span() {
    val span = DialPreviewFixtures.London.lightSpans.first()
    SkywardPreview {
        DetailCard(
            title = "Golden Hour (Dawn)",
            timing = "3:55 AM – 5:37 AM",
            duration = "1h 42m",
            description = SkyItem.Light(span).description,
            swatch = DialPalette.GoldenHour.toColor(),
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun Preview_DetailCard_Event() {
    SkywardPreview {
        DetailCard(
            title = "Sunset",
            timing = "9:21 PM",
            duration = null,
            description =
                "The top edge of the sun sets below the horizon. This is colloquially known as " +
                    "\"Sunset\".",
            swatch = Color.Unspecified,
            onDismiss = {},
        )
    }
}
