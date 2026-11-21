package app.skyward.sky.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.skyward.design.SkywardPreview

/** What Skyward is, who it builds on, and its licence. */
@Composable
fun AboutTab(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "What is this?",
            style = MaterialTheme.typography.titleMedium,
        )
        Paragraph(
            "Skyward shows today's sun and moon on a 24-hour dial: the sun's phases from night " +
                "through twilight to day, golden and blue hour, and when the moon is up. Tap a " +
                "ring, a marker or a row to learn more about it.",
        )
        Paragraph(
            "Sun and moon calculations by Kastro (Apache-2.0) and design based on kastro-demo " +
                "(GPL-3.0), both by James Yox.",
        )
        Paragraph("Skyward is free software under the GPL-3.0.")
        Paragraph("Source code: (link coming soon)")
    }
}

@Composable
private fun Paragraph(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun Preview_AboutTab() {
    SkywardPreview { AboutTab() }
}
