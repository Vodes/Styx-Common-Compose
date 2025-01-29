package moe.styx.common.compose.components.buttons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun ExpandIconButton(
    modifier: Modifier = Modifier,
    tooltip: String? = null,
    tooltipExpanded: String? = null,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    isExpanded: Boolean,
    onClick: () -> Unit = {}
) {
    if (!tooltip.isNullOrBlank() && !tooltipExpanded.isNullOrBlank()) {
        IconButtonWithTooltip(
            if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            if (isExpanded) tooltipExpanded else tooltip,
            tint = tint,
            modifier = modifier,
            onClick = onClick
        )
    } else {
        IconButton(onClick, modifier = modifier) {
            Icon(if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, "", tint = tint)
        }
    }
}

@Composable
expect fun ToolTipWrapper(text: String, modifier: Modifier = Modifier, content: @Composable () -> Unit)

@Composable
fun IconButtonWithTooltip(
    icon: ImageVector,
    tooltip: String,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    ToolTipWrapper(tooltip) {
        IconButton(onClick, modifier = modifier, enabled = enabled, colors = colors) {
            Icon(icon, tooltip, tint = tint, modifier = iconModifier)
        }
    }
}