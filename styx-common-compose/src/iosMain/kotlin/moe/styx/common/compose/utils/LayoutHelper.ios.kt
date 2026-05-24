package moe.styx.common.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import kotlin.math.roundToInt

@Composable
actual fun fetchWindowSize(): LayoutSizes {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    return LayoutSizes(
        (windowInfo.containerSize.width / density.density).roundToInt(),
        (windowInfo.containerSize.height / density.density).roundToInt()
    )
}
