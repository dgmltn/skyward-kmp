package app.skyward.app

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

@Suppress("FunctionNaming")
fun MainViewController(appGroup: String, reloadWidgets: () -> Unit): UIViewController {
    val modules = skywardModules(iosPlatformModule(appGroup, reloadWidgets))
    return ComposeUIViewController { SkywardRoot(modules) }
}
