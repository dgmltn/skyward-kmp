package app.skyward.domain

import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow

data class SavedLocation(val point: GeoPoint, val fixedAt: Instant)

sealed interface FixResult {
    data class Success(val location: SavedLocation) : FixResult

    data object PermissionDenied : FixResult

    data object Unavailable : FixResult
}

interface LocationRepository {
    val saved: Flow<SavedLocation?>

    /** Asks the platform for a coarse fix and saves it on success. */
    suspend fun refresh(): FixResult
}
