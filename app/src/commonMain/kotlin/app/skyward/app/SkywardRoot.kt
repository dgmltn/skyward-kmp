package app.skyward.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.skyward.design.SkywardTheme
import app.skyward.sky.SkyRoute
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.Module

@Composable
fun SkywardRoot(modules: List<Module>, modifier: Modifier = Modifier) {
    KoinApplication(application = { modules(modules) }) {
        SkywardTheme {
            // The Surface provides the content color that un-themed Text inherits.
            Surface(
                modifier = modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                SkyRoute(viewModel = koinViewModel())
            }
        }
    }
}
