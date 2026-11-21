package app.skyward.sky

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.skyward.design.SkywardPreview

/** Stands in for the dial when the sky couldn't be worked out for the saved location. */
@Composable
fun SkyUnavailable(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Sky times unavailable",
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Preview
@Composable
private fun Preview_SkyUnavailable() {
    SkywardPreview { SkyUnavailable(modifier = Modifier.size(360.dp)) }
}
