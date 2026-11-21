package app.skyward.sky.tabs

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import app.skyward.domain.SkyDay
import app.skyward.domain.SkyItem
import app.skyward.sky.dial.toColor
import app.skyward.sky.duration
import app.skyward.sky.format
import app.skyward.sky.ringColor
import app.skyward.sky.timing
import app.skyward.sky.title
import app.skyward.sky.wantsDarkText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/** One span in the States tab, drawn in its dial ring colour. */
@Immutable
data class SpanRow(
    val item: SkyItem,
    val title: String,
    val times: String,
    val duration: String,
    val color: Color,
    val darkText: Boolean,
)

/** The sun's phases and light phases, interleaved by start time. */
internal fun SkyDay.sunRows(use24Hour: Boolean): ImmutableList<SpanRow> =
    (solarPhases.map { it.start to SkyItem.Solar(it) } +
            lightSpans.map { it.start to SkyItem.Light(it) })
        .sortedBy { (start, _) -> start }
        .map { (_, item) -> item.toSpanRow(this, use24Hour) }
        .toImmutableList()

internal fun SkyDay.moonRows(use24Hour: Boolean): ImmutableList<SpanRow> =
    moonSpans.map { SkyItem.Moon(it).toSpanRow(this, use24Hour) }.toImmutableList()

private fun SkyItem.toSpanRow(day: SkyDay, use24Hour: Boolean): SpanRow {
    val color = checkNotNull(ringColor) { "Only spans have a ring colour" }
    return SpanRow(
        item = this,
        title = title,
        times = timing.format(day.timeZone, use24Hour),
        duration = timing.duration.orEmpty(),
        color = color.toColor(),
        darkText = color.wantsDarkText,
    )
}
