package app.skyward.sky.tabs

import app.skyward.sky.formatDuration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/** The time until [body]'s next rise or set, whichever is sooner, like "Sunset in 3h 20m". */
internal fun countdown(body: String, now: Instant, rise: Instant?, set: Instant?): String {
    val riseName = "${body}rise"
    val setName = "${body}set"
    val (name, at) =
        when {
            rise != null && (set == null || rise < set) -> riseName to rise
            set != null -> setName to set
            else -> return "No ${riseName.lowercase()} or ${setName.lowercase()} soon"
        }
    val left = at - now
    return if (left < 1.minutes) "$name in under a minute" else "$name in ${formatDuration(left)}"
}
