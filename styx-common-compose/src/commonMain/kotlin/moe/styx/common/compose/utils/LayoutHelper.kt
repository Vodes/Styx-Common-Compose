package moe.styx.common.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import moe.styx.common.Platform

data class LayoutSizes(val width: Int, val height: Int) {
    val isLandScape = width > height
    val isMedium = width > 600

    val isWide = isMedium || isLandScape || Platform.current == Platform.JVM // Always default to wide on Desktop

    val isProbablyTablet: Boolean
        get() {
            return if (isLandScape) height > 600
            else width > 600
        }
}

@Composable
expect fun fetchWindowSize(): LayoutSizes

val LocalLayoutSize: ProvidableCompositionLocal<LayoutSizes> =
    staticCompositionLocalOf { error("LayoutSizes not initialized") }