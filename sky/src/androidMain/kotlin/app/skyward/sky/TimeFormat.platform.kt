package app.skyward.sky

import java.text.DateFormat
import java.text.SimpleDateFormat

/** Android keeps `java.text`'s short time pattern in step with the user's 12/24-hour setting. */
actual fun is24HourClock(): Boolean {
    val pattern =
        (DateFormat.getTimeInstance(DateFormat.SHORT) as? SimpleDateFormat)?.toPattern()
            ?: return false
    return 'H' in pattern || 'k' in pattern
}
