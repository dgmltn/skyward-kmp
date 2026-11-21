package app.skyward.sky

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Emits once at each wall-clock minute boundary, starting with the next one. */
fun minuteTicker(clock: Clock): Flow<Unit> = flow {
    while (true) {
        delay(clock.now().untilNextMinute())
        emit(Unit)
    }
}

private fun Instant.untilNextMinute(): Duration {
    val intoMinute = (epochSeconds % SECONDS_PER_MINUTE).seconds + nanosecondsOfSecond.nanoseconds
    return 1.minutes - intoMinute
}

private const val SECONDS_PER_MINUTE = 60
