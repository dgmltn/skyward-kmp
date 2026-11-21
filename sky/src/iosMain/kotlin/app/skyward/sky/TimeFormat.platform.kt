package app.skyward.sky

import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale

/** The "j" template resolves to the locale's hour format with the user's 12/24-hour override. */
actual fun is24HourClock(): Boolean {
    val pattern =
        NSDateFormatter.dateFormatFromTemplate("j", options = 0u, locale = NSLocale.currentLocale)
            ?: return false
    return 'H' in pattern || 'k' in pattern
}
