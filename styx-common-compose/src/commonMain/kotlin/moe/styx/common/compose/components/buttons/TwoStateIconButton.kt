package moe.styx.common.compose.components.buttons

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import moe.styx.common.compose.settings

@Composable
fun TwoStateIconButton(
    key: String,
    default: Boolean,
    iconTrue: ImageVector,
    iconFalse: ImageVector,
    onChange: (Boolean) -> Unit = {},
    trueTooltip: String? = null,
    falseTooltip: String? = null
) {
    var setting by remember { mutableStateOf(settings[key, default]) }
    if (!trueTooltip.isNullOrBlank() && !falseTooltip.isNullOrBlank()) {
        IconButtonWithTooltip(if (setting) iconTrue else iconFalse, if (setting) trueTooltip else falseTooltip) {
            settings[key] = !setting
            setting = !setting
            onChange(setting)
        }
    } else {
        IconButton({
            settings[key] = !setting
            setting = !setting
            onChange(setting)
        }) {
            Icon(if (setting) iconTrue else iconFalse, "")
        }
    }
}