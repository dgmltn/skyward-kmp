package app.skyward.data

import dev.jamesyox.kastro.sol.SolarEvent
import dev.jamesyox.kastro.sol.SolarEventSequence
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.time.Instant

class KastroResolvesTest {
    @Test
    fun sunriseExistsInLondonOnSolstice() {
        val first =
            SolarEventSequence(
                    start = Instant.parse("2026-06-21T00:00:00Z"),
                    latitude = 51.5074,
                    longitude = -0.1278,
                    requestedSolarEvents = listOf(SolarEvent.Sunrise),
                )
                .first()

        assertIs<SolarEvent.Sunrise>(first)
    }
}
