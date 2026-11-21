package app.skyward.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SkywardColorScheme =
    darkColorScheme(
        primary = SkywardColors.Accent,
        onPrimary = SkywardColors.TextPrimary,
        primaryContainer = SkywardColors.Accent,
        onPrimaryContainer = SkywardColors.TextPrimary,
        background = SkywardColors.Background,
        onBackground = SkywardColors.TextPrimary,
        surface = SkywardColors.Panel,
        onSurface = SkywardColors.TextPrimary,
        surfaceVariant = SkywardColors.Card,
        onSurfaceVariant = SkywardColors.TextSecondary,
        surfaceContainer = SkywardColors.Panel,
        surfaceContainerHigh = SkywardColors.Card,
    )

@Composable
fun SkywardTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SkywardColorScheme,
        content = content,
    )
}
