package moe.styx.common.compose.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset

@Composable
actual fun StyxCurrentScreenPredictiveBack(
    navigator: Navigator,
    slideAnimationSpec: FiniteAnimationSpec<IntOffset>,
    enabled: Boolean,
    content: @Composable AnimatedVisibilityScope.(Screen) -> Unit
) {
    StyxCurrentScreen(
        navigator = navigator,
        slideAnimationSpec = slideAnimationSpec,
        content = content
    )
}
