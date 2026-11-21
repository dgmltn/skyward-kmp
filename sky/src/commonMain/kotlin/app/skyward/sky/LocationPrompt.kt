package app.skyward.sky

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.skyward.design.SkywardPreview

/** Stands in for the dial until there's a location, with a way to grant one. */
@Composable
fun LocationPrompt(
    denied: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
    locating: Boolean = false,
    couldNotLocate: Boolean = false,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = if (locating) "Finding your location…" else "Location needed",
                style = MaterialTheme.typography.titleLarge,
            )
            when {
                locating -> CircularProgressIndicator()
                else -> {
                    if (couldNotLocate && !denied) {
                        Text(
                            text =
                                "Couldn't get your location. Check that Location is on, then try again.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Button(onClick = onRequestPermission) {
                        Text(
                            when {
                                denied -> "Open Settings"
                                couldNotLocate -> "Try again"
                                else -> "Allow location"
                            },
                        )
                    }
                }
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

@Preview
@Composable
private fun Preview_LocationPrompt_Locating() {
    SkywardPreview {
        LocationPrompt(
            denied = false,
            locating = true,
            onRequestPermission = {},
            modifier = Modifier.size(360.dp),
        )
    }
}

@Preview
@Composable
private fun Preview_LocationPrompt_CouldNotLocate() {
    SkywardPreview {
        LocationPrompt(
            denied = false,
            couldNotLocate = true,
            onRequestPermission = {},
            modifier = Modifier.size(360.dp),
        )
    }
}
