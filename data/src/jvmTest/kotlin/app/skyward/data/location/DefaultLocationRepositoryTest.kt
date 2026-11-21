package app.skyward.data.location

import app.skyward.domain.FixResult
import app.skyward.domain.GeoPoint
import app.skyward.domain.SavedLocation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class DefaultLocationRepositoryTest {
    private val denver =
        SavedLocation(GeoPoint(39.7392, -104.9903), Instant.fromEpochMilliseconds(1_000))
    private val london =
        SavedLocation(GeoPoint(51.5074, -0.1278), Instant.fromEpochMilliseconds(2_000))

    private class InMemoryStore(initial: SavedLocation?) : LastLocationStore {
        private val state = MutableStateFlow(initial)
        override val saved: Flow<SavedLocation?> = state

        override suspend fun save(location: SavedLocation) {
            state.value = location
        }
    }

    private var savedNotifications = 0

    private fun repository(result: FixResult, previous: SavedLocation? = null) =
        DefaultLocationRepository(
            source = { result },
            store = InMemoryStore(previous),
            onSaved = { savedNotifications++ },
        )

    @Test
    fun successfulFixIsSavedAndNotifies() = runTest {
        val repository = repository(FixResult.Success(london))

        val result = repository.refresh()

        assertEquals(FixResult.Success(london), result)
        assertEquals(london, repository.saved.first())
        assertEquals(1, savedNotifications)
    }

    @Test
    fun deniedFixKeepsPreviousLocation() = runTest {
        val repository = repository(FixResult.PermissionDenied, previous = denver)

        assertEquals(FixResult.PermissionDenied, repository.refresh())
        assertEquals(denver, repository.saved.first())
        assertEquals(0, savedNotifications)
    }

    @Test
    fun unavailableFixKeepsPreviousLocation() = runTest {
        val repository = repository(FixResult.Unavailable, previous = denver)

        assertEquals(FixResult.Unavailable, repository.refresh())
        assertEquals(denver, repository.saved.first())
        assertEquals(0, savedNotifications)
    }
}
