package app.skyward.sky.tabs

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** Rings a selected row in [color], leaving the same gap round an unselected one. */
internal fun Modifier.selectionRing(selected: Boolean, color: Color, shape: Shape): Modifier =
    then(if (selected) Modifier.border(2.dp, color, shape) else Modifier).padding(3.dp)
