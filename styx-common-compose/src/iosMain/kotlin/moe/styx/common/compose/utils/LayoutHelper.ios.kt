package moe.styx.common.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalWindowInfo

@Composable
actual fun fetchWindowSize(): LayoutSizes {
    val windowInfo = LocalWindowInfo.current
    return LayoutSizes(windowInfo.containerSize.width, windowInfo.containerSize.height)
}