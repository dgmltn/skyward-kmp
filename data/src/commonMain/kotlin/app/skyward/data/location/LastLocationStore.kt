package app.skyward.data.location

import app.skyward.domain.FixResult
import app.skyward.domain.SavedLocation
import kotlinx.coroutines.flow.Flow

interface LastLocationStore {
    val saved: Flow<SavedLocation?>

    suspend fun save(location: SavedLocation)
}

fun interface LocationSource {
    suspend fun currentFix(): FixResult
}
