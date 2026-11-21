package app.skyward.sky.tabs

import androidx.compose.runtime.Immutable
import app.skyward.domain.SkyDay
import app.skyward.domain.SkyItem
import app.skyward.sky.description
import app.skyward.sky.formatTime
import app.skyward.sky.title
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/** One solar or lunar event in the Events tab. */
@Immutable
data class EventRow(
    val item: SkyItem,
    val title: String,
    val time: String,
    val description: String,
)

/** Today's solar and lunar events in time order. */
internal fun SkyDay.eventRows(use24Hour: Boolean): ImmutableList<EventRow> =
    (solarEvents.map { it.time to SkyItem.SolarEventItem(it) } +
            lunarEvents.map { it.time to SkyItem.LunarEventItem(it) })
        .sortedBy { (time, _) -> time }
        .map { (time, item) ->
            EventRow(
                item = item,
                title = item.title,
                time = formatTime(time, timeZone, use24Hour),
                description = item.description,
            )
        }
        .toImmutableList()
