package app.skyward.data.location

import app.skyward.domain.FixResult
import app.skyward.domain.LocationRepository
import app.skyward.domain.SavedLocation
import kotlinx.coroutines.flow.Flow

class DefaultLocationRepository(
    private val source: LocationSource,
    private val store: LastLocationStore,
    private val onSaved: () -> Unit = {},
) : LocationRepository {
    override val saved: Flow<SavedLocation?> = store.saved

    override suspend fun refresh(): FixResult {
        val result = source.currentFix()
        if (result is FixResult.Success) {
            store.save(result.location)
            onSaved()
        }
        return result
    }
}
