package app.skyward.app

import app.skyward.data.sky.KastroSkyRepository
import app.skyward.domain.SkyRepository
import app.skyward.sky.SkyViewModel
import app.skyward.sky.is24HourClock
import app.skyward.sky.minuteTicker
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** The shared graph plus the [platform] bindings, which must provide a `LocationRepository`. */
fun skywardModules(platform: Module): List<Module> = listOf(platform, sharedModule)

private val sharedModule = module {
    single<Clock> { Clock.System }
    single<SkyRepository> { KastroSkyRepository() }
    viewModel {
        val clock: Clock = get()
        SkyViewModel(
            location = get(),
            sky = get(),
            clock = clock,
            timeZone = { TimeZone.currentSystemDefault() },
            ticker = minuteTicker(clock),
            use24Hour = ::is24HourClock,
        )
    }
}
