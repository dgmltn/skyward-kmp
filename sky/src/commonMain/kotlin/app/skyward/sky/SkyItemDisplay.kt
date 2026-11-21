package app.skyward.sky

import app.skyward.dial.DialColor
import app.skyward.dial.DialPalette
import app.skyward.domain.SkyItem
import kotlin.time.Instant
import kotlinx.datetime.TimeZone

/** The span's ring colour on the dial, or null for an event. */
internal val SkyItem.ringColor: DialColor?
    get() =
        when (this) {
            is SkyItem.Solar -> DialPalette.of(span.phase)
            is SkyItem.Light -> DialPalette.of(span.lightState)
            is SkyItem.Moon -> DialPalette.of(span.horizonState)
            is SkyItem.SolarEventItem,
            is SkyItem.LunarEventItem -> null
        }

/** Whether text on this ring colour should be dark, as the demo's tables do. */
internal val DialColor.wantsDarkText: Boolean
    get() = luminance > DARK_TEXT_LUMINANCE

/** Start, end and time of an item: an event has only a time. */
internal sealed interface ItemTiming {
    data class Span(val start: Instant, val end: Instant) : ItemTiming

    data class Moment(val at: Instant) : ItemTiming
}

internal val SkyItem.timing: ItemTiming
    get() =
        when (this) {
            is SkyItem.Solar -> ItemTiming.Span(span.start, span.end)
            is SkyItem.Light -> ItemTiming.Span(span.start, span.end)
            is SkyItem.Moon -> ItemTiming.Span(span.start, span.end)
            is SkyItem.SolarEventItem -> ItemTiming.Moment(event.time)
            is SkyItem.LunarEventItem -> ItemTiming.Moment(event.time)
        }

/** Like "4:50 AM – 9:14 PM" for a span, or "1:02 PM" for an event. */
internal fun ItemTiming.format(timeZone: TimeZone, use24Hour: Boolean): String =
    when (this) {
        is ItemTiming.Span ->
            "${formatTime(start, timeZone, use24Hour)} – ${formatTime(end, timeZone, use24Hour)}"
        is ItemTiming.Moment -> formatTime(at, timeZone, use24Hour)
    }

internal val ItemTiming.duration: String?
    get() =
        when (this) {
            is ItemTiming.Span -> formatDuration(end - start)
            is ItemTiming.Moment -> null
        }

private const val DARK_TEXT_LUMINANCE = 0.6f
