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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.skyward.design.SkywardPreview
import app.skyward.domain.SkyItem
import app.skyward.sky.description
import app.skyward.sky.dial.DialPreviewFixtures
import app.skyward.sky.title
import kotlinx.collections.immutable.ImmutableList

/** Today's solar and lunar events in time order. */
@Composable
fun EventsTab(
    rows: ImmutableList<EventRow>,
    selected: SkyItem?,
    onSelect: (SkyItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        rows.forEach { row ->
            EventRowItem(
                row = row,
                selected = row.item == selected,
                onClick = { onSelect(row.item) },
            )
        }
    }
}

@Composable
private fun EventRowItem(
    row: EventRow,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(6.dp)
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .selectionRing(selected, MaterialTheme.colorScheme.onSurface, shape)
                .background(
                    if (selected) MaterialTheme.colorScheme.surfaceContainerHigh
                    else Color.Transparent,
                    shape,
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = row.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = row.time,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview
@Composable
private fun Preview_EventsTab() {
    SkywardPreview {
        EventsTab(
            rows = DialPreviewFixtures.London.eventRows(use24Hour = false),
            selected = null,
            onSelect = {},
        )
    }
}

@Preview
@Composable
private fun Preview_EventsTab_Selected() {
    val day = DialPreviewFixtures.London
    SkywardPreview {
        EventsTab(
            rows = day.eventRows(use24Hour = true),
            selected = SkyItem.SolarEventItem(day.solarEvents.first { it.title == "Sunset" }),
            onSelect = {},
        )
    }
}
