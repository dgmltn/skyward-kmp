package app.skyward.app

import app.skyward.data.location.DefaultLocationRepository
import app.skyward.data.location.IosLocationSource
import app.skyward.data.location.UserDefaultsLastLocationStore
import app.skyward.domain.LocationRepository
import org.koin.core.module.Module
import org.koin.dsl.module

/** iOS bindings; [reloadWidgets] runs after each saved fix so the widget picks it up. */
fun iosPlatformModule(appGroup: String, reloadWidgets: () -> Unit): Module = module {
    single<LocationRepository> {
        DefaultLocationRepository(
            source = IosLocationSource(),
            store = UserDefaultsLastLocationStore(appGroup),
            onSaved = reloadWidgets,
        )
    }
}
