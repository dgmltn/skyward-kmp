package app.skyward.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/** Every icon the app draws. Paths are Material Symbols on a 24x24 grid. */
enum class SkywardIcon(private val pathData: String) {
    Close(
        "M19,6.41L17.59,5 12,10.59 6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 12,13.41 " +
            "17.59,19 19,17.59 13.41,12z",
    );

    private val vector: ImageVector by lazy {
        ImageVector.Builder(
                name = name,
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
            .addPath(
                pathData = PathParser().parsePathString(pathData).toNodes(),
                fill = SolidColor(Color.Black),
            )
            .build()
    }

    val painter: Painter
        @Composable get() = rememberVectorPainter(vector)

    @Composable
    operator fun invoke(
        modifier: Modifier = Modifier,
        contentDescription: String? = null,
        tint: Color = LocalContentColor.current,
    ) {
        SkywardIcon(
            icon = this,
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint,
        )
    }
}

/** Renders a [SkywardIcon] via Material 3 [Icon]. */
@Composable
fun SkywardIcon(
    icon: SkywardIcon,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    Icon(
        painter = icon.painter,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
    )
}

@Preview
@Composable
private fun Preview_SkywardIconGallery() {
    SkywardPreview {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SkywardIcon.entries.forEach { icon ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    icon()
                    Text(icon.name)
                }
            }
        }
    }
}
