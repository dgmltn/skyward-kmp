package app.skyward.android

import android.app.Application
import app.skyward.app.androidPlatformModule
import app.skyward.app.skywardModules

class SkywardApplication : Application() {
    /** Built once per process: the DataStore inside must not be opened twice. */
    val modules by lazy { skywardModules(androidPlatformModule(this)) }
}
