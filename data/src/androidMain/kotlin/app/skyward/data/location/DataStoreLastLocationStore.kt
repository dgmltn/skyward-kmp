package app.skyward.data.location

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import app.skyward.domain.GeoPoint
import app.skyward.domain.SavedLocation
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreLastLocationStore(private val dataStore: DataStore<Preferences>) :
    LastLocationStore {
    override val saved: Flow<SavedLocation?> =
        dataStore.data.map { preferences ->
            val latitude = preferences[LATITUDE]
            val longitude = preferences[LONGITUDE]
            val fixedAt = preferences[FIXED_AT_EPOCH_MS]
            if (latitude == null || longitude == null || fixedAt == null) {
                null
            } else {
                SavedLocation(
                    point = GeoPoint(latitude, longitude),
                    fixedAt = Instant.fromEpochMilliseconds(fixedAt),
                )
            }
        }

    override suspend fun save(location: SavedLocation) {
        dataStore.edit { preferences ->
            preferences[LATITUDE] = location.point.latitude
            preferences[LONGITUDE] = location.point.longitude
            preferences[FIXED_AT_EPOCH_MS] = location.fixedAt.toEpochMilliseconds()
        }
    }

    private companion object {
        val LATITUDE = doublePreferencesKey("lat")
        val LONGITUDE = doublePreferencesKey("lon")
        val FIXED_AT_EPOCH_MS = longPreferencesKey("fixedAtEpochMs")
    }
}
