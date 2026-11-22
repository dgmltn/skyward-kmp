package app.skyward.sky.tabs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class CountdownTest {
    private val now = Instant.parse("2026-11-21T12:00:00Z")

    @Test
    fun theSoonerOfRiseAndSetIsCountedDown() {
        assertEquals(
            "Sunset in 3h 20m",
            countdown("Sun", now, rise = now + 18.hours, set = now + 3.hours + 20.minutes),
        )
    }

    @Test
    fun riseIsUsedWhenThereIsNoSet() {
        assertEquals("Moonrise in 45m", countdown("Moon", now, rise = now + 45.minutes, set = null))
    }

    @Test
    fun underAMinuteIsSaidInWords() {
        assertEquals(
            "Sunrise in under a minute",
            countdown("Sun", now, rise = now + 30.seconds, set = null),
        )
    }

    @Test
    fun neitherSoonSaysSo() {
        assertEquals("No sunrise or sunset soon", countdown("Sun", now, rise = null, set = null))
    }
}
