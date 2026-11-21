package app.skyward.sky

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.skyward.design.SkywardPreview

/** Stands in for the dial until there's a location, with a way to grant one. */
@Composable
fun LocationPrompt(
    denied: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Location needed",
                style = MaterialTheme.typography.titleLarge,
            )
            Button(onClick = onRequestPermission) {
                Text(if (denied) "Open Settings" else "Allow location")
            }
        }
    }
}

@Preview
@Composable
private fun Preview_LocationPrompt() {
    SkywardPreview {
        LocationPrompt(
            denied = false,
            onRequestPermission = {},
            modifier = Modifier.size(360.dp),
        )
    }
}

@Preview
@Composable
private fun Preview_LocationPrompt_Denied() {
    SkywardPreview {
        LocationPrompt(
            denied = true,
            onRequestPermission = {},
            modifier = Modifier.size(360.dp),
        )
    }
}
