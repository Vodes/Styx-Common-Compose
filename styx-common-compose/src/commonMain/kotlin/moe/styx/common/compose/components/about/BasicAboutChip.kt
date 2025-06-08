package moe.styx.common.compose.components.about

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun BasicChip(
    text: String,
    icon: ImageVector? = null,
    iconDescription: String? = null,
    color: Color = MaterialTheme.colorScheme.tertiaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier.padding(5.dp, 0.dp),
    textWidth: Dp = Dp.Unspecified,
    onClick: () -> Unit = {}
) {
    val painter = icon?.let { rememberVectorPainter(it) }
    BasicChipPainter(text, painter, iconDescription, color, contentColor, modifier, textWidth, Dp.Unspecified, onClick)
}

@Composable
internal fun BasicChipPainter(
    text: String,
    icon: Painter? = null,
    iconDescription: String? = null,
    color: Color = MaterialTheme.colorScheme.tertiaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier.padding(5.dp, 0.dp),
    textWidth: Dp = Dp.Unspecified,
    enforceSize: Dp = Dp.Unspecified,
    onClick: () -> Unit = {}
) {
    val chipColors = AssistChipDefaults.assistChipColors(containerColor = color)
    AssistChip(onClick, label = {
        if (textWidth != Dp.Unspecified)
            Text(text, color = contentColor, textAlign = TextAlign.Center, modifier = Modifier.width(textWidth))
        else
            Text(text, color = contentColor, textAlign = TextAlign.Center)
    }, leadingIcon = {
        if (icon != null && iconDescription != null)
            Icon(
                icon,
                iconDescription,
                tint = contentColor,
                modifier = if (enforceSize == Dp.Unspecified) Modifier else Modifier.size(enforceSize)
            )
    }, colors = chipColors, modifier = modifier, border = null)
}