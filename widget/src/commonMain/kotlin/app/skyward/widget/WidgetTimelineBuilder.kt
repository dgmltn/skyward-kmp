package app.skyward.widget

import app.skyward.dial.buildDialModel
import app.skyward.domain.SavedLocation
import app.skyward.domain.SkyDay
import app.skyward.domain.SkyRepository
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.datetime.IllegalTimeZoneException
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class WidgetTimelineBuilder(private val sky: SkyRepository) {
    @Suppress("LongParameterList")
    fun build(
        location: SavedLocation?,
        nowEpochMillis: Long,
        timeZoneId: String,
        use24Hour: Boolean,
        stepMinutes: Int = 5,
        count: Int = 72,
    ): WidgetTimeline {
        if (location == null) return WidgetTimeline.NoLocation
        val timeZone = timeZoneOrSystemDefault(timeZoneId)
        val start = Instant.fromEpochMilliseconds(nowEpochMillis)
        val days = mutableMapOf<LocalDate, SkyDay>()
        val entries =
            List(count) { index ->
                val instant = start + (stepMinutes * index).minutes
                val date = instant.toLocalDateTime(timeZone).date
                val day = days.getOrPut(date) { sky.day(location.point, date, timeZone) }
                val now = sky.now(location.point, instant)
                WidgetEntry(
                    instant.toEpochMilliseconds(),
                    buildDialModel(day, now, null, use24Hour),
                )
            }
        return WidgetTimeline.Entries(entries)
    }
}

private fun timeZoneOrSystemDefault(id: String): TimeZone =
    try {
        TimeZone.of(id)
    } catch (_: IllegalTimeZoneException) {
        TimeZone.currentSystemDefault()
    }
