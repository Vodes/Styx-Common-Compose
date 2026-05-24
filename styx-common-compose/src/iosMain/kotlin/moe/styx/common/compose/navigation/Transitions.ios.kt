package moe.styx.common.compose.navigation

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import io.github.hristogochev.vortex.screen.CurrentScreenIOSSwipe

@Composable
actual fun StyxCurrentScreenPredictiveBack(
    navigator: Navigator,
    slideAnimationSpec: FiniteAnimationSpec<IntOffset>,
    enabled: Boolean,
    content: @Composable (Screen) -> Unit
) {
    CurrentScreenIOSSwipe(
        navigator = navigator,
        enableBackHandler = enabled,
        content = content
    )
}
