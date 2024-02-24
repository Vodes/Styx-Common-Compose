package moe.styx.common.compose.components.misc

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import moe.styx.common.compose.components.AppShapes

@Composable
fun OutlinedText(text: String, lineColor: Color = MaterialTheme.colorScheme.primary, fillColor: Color = MaterialTheme.colorScheme.surface) {
    Surface(
        Modifier.padding(2.dp, 2.dp).sizeIn(0.dp, 36.dp).clip(AppShapes.medium),
        shape = AppShapes.medium,
        border = BorderStroke(2.dp, lineColor),
        color = fillColor,
    ) {
        Row {
            Text(text, Modifier.padding(7.dp).align(Alignment.CenterVertically))
        }
    }
}

@Composable
fun PrimarySelectableObject(name: String, isSelected: MutableState<Boolean>, onSelection: (Boolean) -> Unit) =
    SelectableObject(
        name,
        isSelected,
        MaterialTheme.colorScheme.onPrimary,
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.surface,
        onSelection
    )

@Composable
fun SecondarySelectableObject(name: String, isSelected: MutableState<Boolean>, onSelection: (Boolean) -> Unit) =
    SelectableObject(
        name,
        isSelected,
        MaterialTheme.colorScheme.onSecondary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.surface,
        onSelection
    )

@Composable
private fun SelectableObject(
    name: String,
    isSelected: MutableState<Boolean>,
    selectedTextColor: Color,
    selectedFillColor: Color,
    defaultTextColor: Color,
    defaultFillColor: Color,
    onSelection: (Boolean) -> Unit
) {
    val textColor by animateColorAsState(if (isSelected.value) selectedTextColor else defaultTextColor)
    val fillColor by animateColorAsState(if (isSelected.value) selectedFillColor else defaultFillColor)

    Surface(
        Modifier.padding(2.dp, 2.dp).sizeIn(0.dp, 36.dp).clip(AppShapes.medium)
            .clickable { isSelected.value = !isSelected.value; onSelection(isSelected.value) },
        shape = AppShapes.medium,
        border = BorderStroke(2.dp, selectedFillColor),
        color = fillColor,
    ) {
        Row {
            Text(
                name,
                Modifier.padding(7.dp).align(Alignment.CenterVertically),
                color = textColor
            )
        }
    }
}
