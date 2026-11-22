/*
 * Based on kastro-demo by James Yox (GPL-3.0)
 * https://github.com/yoxjames/kastro-demo
 */

package app.skyward.sky.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.skyward.design.SkywardPreview
import app.skyward.domain.SkyItem
import app.skyward.sky.dial.DialPreviewFixtures
import app.skyward.sky.duration
import app.skyward.sky.title
import kotlinx.collections.immutable.ImmutableList

/** Today's spans: sun phases and light phases interleaved by start, then the moon's. */
@Composable
fun StatesTab(
    sunRows: ImmutableList<SpanRow>,
    moonRows: ImmutableList<SpanRow>,
    selected: SkyItem?,
    onSelect: (SkyItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        SectionHeader("Sun")
        sunRows.forEach { row ->
            SpanRowItem(
                row = row,
                selected = row.item == selected,
                onClick = { onSelect(row.item) },
            )
        }
        SectionHeader("Moon", Modifier.padding(top = 12.dp))
        moonRows.forEach { row ->
            SpanRowItem(
                row = row,
                selected = row.item == selected,
                onClick = { onSelect(row.item) },
            )
        }
    }
}

@Composable
private fun SpanRowItem(
    row: SpanRow,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(6.dp)
    val textColor = if (row.darkText) Color.Black else Color.White
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .selectionRing(selected, MaterialTheme.colorScheme.onSurface, shape)
                .background(row.color, shape)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = row.title,
            color = textColor,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = row.times,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End,
            )
            Text(
                text = row.duration,
                color = textColor.copy(alpha = SECONDARY_TEXT_ALPHA),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private const val SECONDARY_TEXT_ALPHA = 0.75f

@Preview
@Composable
private fun Preview_StatesTab() {
    val day = DialPreviewFixtures.London
    SkywardPreview {
        StatesTab(
            sunRows = day.sunRows(use24Hour = false),
            moonRows = day.moonRows(use24Hour = false),
            selected = null,
            onSelect = {},
        )
    }
}

@Preview
@Composable
private fun Preview_StatesTab_Selected() {
    val day = DialPreviewFixtures.London
    SkywardPreview {
        StatesTab(
            sunRows = day.sunRows(use24Hour = true),
            moonRows = day.moonRows(use24Hour = true),
            selected = SkyItem.Moon(day.moonSpans.last()),
            onSelect = {},
        )
    }
}
