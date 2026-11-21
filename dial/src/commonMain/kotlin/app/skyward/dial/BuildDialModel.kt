/*
 * Based on kastro-demo by James Yox (GPL-3.0)
 * https://github.com/jamesyox/kastro-demo
 */

package app.skyward.dial

import app.skyward.dial.DialGeometry.angleOf
import app.skyward.domain.SkyDay
import app.skyward.domain.SkyItem
import app.skyward.domain.SkyNow
import dev.jamesyox.kastro.sol.LightState
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

private const val HOURS_PER_DAY = 24
private const val HOURS_PER_HALF_DAY = 12
private const val DIMMED_ALPHA = 0.3f
private const val BLUE_HOUR_RADIUS_OFFSET = -4.0
private const val HOUR_TICK_LENGTH = 5.0
private const val HOUR_TICK_WIDTH = 0.4

fun buildDialModel(day: SkyDay, now: SkyNow?, selected: SkyItem?, use24Hour: Boolean): DialModel {
    fun angle(instant: Instant) = angleOf(instant, day.start, day.end)

    fun arc(
        radius: Double,
        width: Double,
        start: Instant,
        end: Instant,
        color: DialColor,
        item: SkyItem,
    ) =
        DialArc(
            radius = radius,
            width = width,
            startDegrees = angle(start),
            sweepDegrees = angle(end) - angle(start),
            color = color,
            alpha =
                if (selected == null || selected.isEvent() || selected == item) 1f
                else DIMMED_ALPHA,
            item = item,
        )

    val solarArcs =
        day.solarPhases.map {
            arc(
                DialGeometry.SOLAR_RADIUS,
                DialGeometry.SOLAR_WIDTH,
                it.start,
                it.end,
                DialPalette.of(it.phase),
                SkyItem.Solar(it),
            )
        }
    val lightArcs =
        day.lightSpans.map {
            val offset = if (it.lightState.isBlueHour()) BLUE_HOUR_RADIUS_OFFSET else 0.0
            arc(
                DialGeometry.LIGHT_RADIUS + offset,
                DialGeometry.LIGHT_WIDTH,
                it.start,
                it.end,
                DialPalette.of(it.lightState),
                SkyItem.Light(it),
            )
        }
    val moonArcs =
        day.moonSpans.map {
            arc(
                DialGeometry.MOON_RADIUS,
                DialGeometry.MOON_WIDTH,
                it.start,
                it.end,
                DialPalette.of(it.horizonState),
                SkyItem.Moon(it),
            )
        }

    val hours = day.hourInstants()
    val markers = day.markers(::angle)

    return DialModel(
        arcs = solarArcs + lightArcs + moonArcs,
        hourTicks = hours.map { hourTick(angle(it.instant)) },
        hourLabels = hours.map { hourLabel(angle(it.instant), it.hour, use24Hour) },
        markers = markers,
        selectedEventTick = selected?.eventTime()?.let { selectedEventTick(angle(it)) },
        hand = now?.let { DialHand(angle(it.instant), DialGeometry.HAND_LENGTH, DialPalette.Hand) },
        moonIcon =
            now?.let {
                DialMoonIcon(
                    angleDegrees = angle(it.instant),
                    radius = DialGeometry.MOON_ICON_RADIUS,
                    size = DialGeometry.MOON_ICON_SIZE,
                    phaseDegrees = it.moonPhaseDegrees,
                )
            },
    )
}

private data class HourInstant(val instant: Instant, val hour: Int)

/**
 * One entry per wall-clock hour that exists on the day, plus the closing midnight. Hours skipped by
 * a spring-forward gap resolve to a different wall-clock time and are dropped.
 */
private fun SkyDay.hourInstants(): List<HourInstant> {
    val opened =
        (0..<HOURS_PER_DAY).mapNotNull { hour ->
            val instant = LocalDateTime(date, LocalTime(hour, 0)).toInstant(timeZone)
            val local = instant.toLocalDateTime(timeZone)
            if (local.hour == hour && local.minute == 0) HourInstant(instant, hour) else null
        }
    return opened + HourInstant(end, 0)
}

private fun hourText(hour: Int, use24Hour: Boolean): String =
    if (use24Hour) {
        hour.toString().padStart(2, '0')
    } else {
        val display =
            if (hour % HOURS_PER_HALF_DAY == 0) HOURS_PER_HALF_DAY else hour % HOURS_PER_HALF_DAY
        display.toString() + if (hour < HOURS_PER_HALF_DAY) "am" else "pm"
    }

private fun LightState.isBlueHour() =
    this == LightState.BlueHourDawn || this == LightState.BlueHourDusk

private fun hourTick(angle: Double) =
    DialTick(
        angleDegrees = angle,
        innerRadius = DialGeometry.HOUR_TICK_RADIUS - HOUR_TICK_LENGTH / 2,
        outerRadius = DialGeometry.HOUR_TICK_RADIUS + HOUR_TICK_LENGTH / 2,
        width = HOUR_TICK_WIDTH,
        color = DialPalette.Hour,
    )

private fun hourLabel(angle: Double, hour: Int, use24Hour: Boolean) =
    DialLabel(
        angleDegrees = angle,
        radius = DialGeometry.HOUR_LABEL_RADIUS,
        text = hourText(hour, use24Hour),
        color = DialPalette.Hour,
    )
