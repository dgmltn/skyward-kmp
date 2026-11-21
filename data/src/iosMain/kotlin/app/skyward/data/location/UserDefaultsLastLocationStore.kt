package app.skyward.data.location

import app.skyward.domain.GeoPoint
import app.skyward.domain.SavedLocation
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.Foundation.NSUserDefaults

private const val LATITUDE = "lat"
private const val LONGITUDE = "lon"
private const val FIXED_AT_EPOCH_MS = "fixedAtEpochMs"

/** Synchronous read of the saved fix, for the widget extension. */
fun readSavedLocation(appGroup: String): SavedLocation? {
    val defaults = NSUserDefaults(suiteName = appGroup)
    val fixedAt = defaults.objectForKey(FIXED_AT_EPOCH_MS) as? Number ?: return null
    return SavedLocation(
        point = GeoPoint(defaults.doubleForKey(LATITUDE), defaults.doubleForKey(LONGITUDE)),
        fixedAt = Instant.fromEpochMilliseconds(fixedAt.toLong()),
    )
}

class UserDefaultsLastLocationStore(private val appGroup: String) : LastLocationStore {
    private val state = MutableStateFlow(readSavedLocation(appGroup))

    override val saved: Flow<SavedLocation?> = state

    override suspend fun save(location: SavedLocation) {
        val defaults = NSUserDefaults(suiteName = appGroup)
        defaults.setDouble(location.point.latitude, forKey = LATITUDE)
        defaults.setDouble(location.point.longitude, forKey = LONGITUDE)
        defaults.setObject(location.fixedAt.toEpochMilliseconds(), forKey = FIXED_AT_EPOCH_MS)
        state.value = location
    }
}
