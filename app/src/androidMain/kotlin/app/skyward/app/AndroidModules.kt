package app.skyward.app

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import app.skyward.data.location.AndroidLocationSource
import app.skyward.data.location.DataStoreLastLocationStore
import app.skyward.data.location.DefaultLocationRepository
import app.skyward.domain.LocationRepository
import okio.Path.Companion.toOkioPath
import org.koin.core.module.Module
import org.koin.dsl.module

private const val DATA_STORE_FILE = "skyward.preferences_pb"

/**
 * Android bindings. The DataStore is created here, not inside a Koin definition, so a second Koin
 * start (activity recreation) cannot open a second DataStore on the same file.
 */
fun androidPlatformModule(context: Context): Module {
    val appContext = context.applicationContext
    val dataStore =
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { appContext.filesDir.resolve(DATA_STORE_FILE).toOkioPath() }
        )
    val repository =
        DefaultLocationRepository(
            source = AndroidLocationSource(appContext),
            store = DataStoreLastLocationStore(dataStore),
        )
    return module { single<LocationRepository> { repository } }
}
