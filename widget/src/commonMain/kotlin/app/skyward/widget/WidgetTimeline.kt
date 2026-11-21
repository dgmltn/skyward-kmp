package app.skyward.widget

import app.skyward.dial.DialModel
import co.touchlab.kermit.Logger

sealed interface WidgetTimeline {
    data object NoLocation : WidgetTimeline

    /** The sky couldn't be worked out, so the widget shows a short message instead of a dial. */
    data object Unavailable : WidgetTimeline

    data class Entries(val entries: List<WidgetEntry>) : WidgetTimeline
}

data class WidgetEntry(val epochMillis: Long, val dial: DialModel)

private val log = Logger.withTag("WidgetTimeline")

/**
 * Runs [build], turning any failure into [WidgetTimeline.Unavailable]. An exception that reached
 * Swift would kill the widget extension and leave the widget blank.
 */
@Suppress("TooGenericExceptionCaught")
fun timelineOrUnavailable(build: () -> WidgetTimeline): WidgetTimeline =
    try {
        build()
    } catch (e: Throwable) {
        log.e(e) { "Couldn't build the widget timeline" }
        WidgetTimeline.Unavailable
    }
