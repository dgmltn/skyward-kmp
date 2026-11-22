package app.skyward.data.location

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FreshOrNewestTest {
    private data class Fix(val name: String, val timeMs: Long)

    private val older = Fix("older", 1_000)
    private val newer = Fix("newer", 2_000)

    @Test
    fun freshFixWins() {
        assertEquals(
            Fix("fresh", 500),
            freshOrNewest(Fix("fresh", 500), listOf(older, newer), Fix::timeMs),
        )
    }

    @Test
    fun withoutFreshFixTheNewestCachedOneIsUsed() {
        assertEquals(newer, freshOrNewest(null, listOf(older, null, newer), Fix::timeMs))
    }

    @Test
    fun nothingAtAllGivesNull() {
        assertNull(freshOrNewest(null, listOf(null, null), Fix::timeMs))
    }

    @Test
    fun aRecentCachedFixIsUsedWithoutWaiting() {
        assertEquals(
            newer,
            recentCached(listOf(older, newer), nowMs = 2_500, maxAgeMs = 1_000, Fix::timeMs),
        )
    }

    @Test
    fun anOldCachedFixMeansWaitingForAFreshOne() {
        assertNull(recentCached(listOf(older, newer), nowMs = 9_000, maxAgeMs = 1_000, Fix::timeMs))
    }
}
