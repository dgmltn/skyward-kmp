package app.skyward.design

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Base of every `@Preview`: the theme on its real background, with padding. */
@Composable
fun SkywardPreview(
    modifier: Modifier = Modifier,
    padding: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    SkywardTheme {
        Surface(modifier = modifier) {
            Box(
                modifier = Modifier.padding(padding),
                content = content,
            )
        }
    }
}
