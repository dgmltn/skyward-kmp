package app.skyward.sky.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.skyward.design.SkywardColors
import app.skyward.design.SkywardPreview

private const val KASTRO_URL = "https://github.com/yoxjames/Kastro"
private const val KASTRO_DEMO_URL = "https://github.com/yoxjames/kastro-demo"
private const val TIME_AND_DATE_URL = "https://www.timeanddate.com/astronomy/golden-hour.html"
private const val SOURCE_URL = "https://github.com/dgmltn/skyward-kmp"

/** What Skyward is, who it builds on, and its licence. */
@Composable
fun AboutTab(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "What is this?",
            style = MaterialTheme.typography.titleMedium,
        )
        Paragraph(
            AnnotatedString(
                "Skyward shows today's sun and moon on a 24-hour dial: the sun's phases from night " +
                "through twilight to day, golden and blue hour, and when the moon is up. Tap a " +
                "ring, a marker or a row to learn more about it.",
            ),
        )
        val linkStyle =
            TextLinkStyles(
                SpanStyle(
                    color = SkywardColors.Link,
                    textDecoration = TextDecoration.Underline,
                ),
            )
        Paragraph(
            buildAnnotatedString {
                append("Sun and moon calculations by ")
                withLink(LinkAnnotation.Url(KASTRO_URL, linkStyle)) { append("Kastro") }
                append(" (Apache-2.0). The dial's design and the descriptions of each phase and ")
                append("event come from ")
                withLink(LinkAnnotation.Url(KASTRO_DEMO_URL, linkStyle)) { append("kastro-demo") }
                append(" (GPL-3.0). Both are by James Yox.")
            },
        )
        Paragraph(
            buildAnnotatedString {
                append("kastro-demo's golden hour and blue hour descriptions draw on ")
                withLink(LinkAnnotation.Url(TIME_AND_DATE_URL, linkStyle)) {
                    append("timeanddate.com")
                }
                append(".")
            },
        )
        Paragraph(AnnotatedString("Skyward is free software under the GPL-3.0."))
        Paragraph(
            buildAnnotatedString {
                append("Source code: ")
                withLink(LinkAnnotation.Url(SOURCE_URL, linkStyle)) { append(SOURCE_URL) }
            },
        )
    }
}

@Composable
private fun Paragraph(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun Preview_AboutTab() {
    SkywardPreview { AboutTab() }
}
