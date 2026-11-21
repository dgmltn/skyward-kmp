package app.skyward.sky

import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Whether the device shows times on a 24-hour clock. */
expect fun is24HourClock(): Boolean

/** The wall-clock time of [instant] in [timeZone], like "13:02" or "1:02 PM". */
fun formatTime(instant: Instant, timeZone: TimeZone, use24Hour: Boolean): String {
    val time = instant.toLocalDateTime(timeZone).time
    val minute = time.minute.toString().padStart(2, '0')
    return if (use24Hour) {
        "${time.hour.toString().padStart(2, '0')}:$minute"
    } else {
        val hour = (time.hour + HOURS_PER_HALF_DAY - 1) % HOURS_PER_HALF_DAY + 1
        val suffix = if (time.hour < HOURS_PER_HALF_DAY) "AM" else "PM"
        "$hour:$minute $suffix"
    }
}

/** Like [formatTime], with the date in front when [instant] doesn't fall on [today]. */
internal fun formatTimeFrom(
    today: LocalDate,
    instant: Instant,
    timeZone: TimeZone,
    use24Hour: Boolean,
): String {
    val time = formatTime(instant, timeZone, use24Hour)
    val date = instant.toLocalDateTime(timeZone).date
    if (date == today) return time
    val month =
        date.month.name.take(MONTH_ABBREVIATION).lowercase().replaceFirstChar { it.uppercase() }
    return "$month ${date.day}, $time"
}

/** Like "16h 24m", or "41m" under an hour. */
internal fun formatDuration(duration: Duration): String {
    val minutes = duration.inWholeMinutes
    val hours = minutes / MINUTES_PER_HOUR
    val rest = minutes % MINUTES_PER_HOUR
    return if (hours == 0L) "${rest}m" else "${hours}h ${rest}m"
}

/** [value] to one decimal place, like "-3.5". */
internal fun formatOneDecimal(value: Double): String {
    val tenths = (value * TENTHS).roundToInt()
    val sign = if (tenths < 0) "-" else ""
    return "$sign${abs(tenths) / TENTHS}.${abs(tenths) % TENTHS}"
}

private const val HOURS_PER_HALF_DAY = 12
private const val MINUTES_PER_HOUR = 60
private const val MONTH_ABBREVIATION = 3
private const val TENTHS = 10
