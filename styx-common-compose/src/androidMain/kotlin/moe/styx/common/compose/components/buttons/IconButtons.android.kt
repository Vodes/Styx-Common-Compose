package moe.styx.common.compose.components.buttons

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun ToolTipWrapper(text: String, modifier: Modifier, content: @Composable () -> Unit) {
    Box { content() }
}