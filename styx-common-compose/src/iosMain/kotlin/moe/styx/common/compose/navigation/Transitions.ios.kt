package moe.styx.common.compose.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntOffset
import io.github.hristogochev.vortex.transitions.IOSSlideTransitionPredictiveBack

@Composable
actual fun StyxCurrentScreenPredictiveBack(
    navigator: Navigator,
    slideAnimationSpec: FiniteAnimationSpec<IntOffset>,
    enabled: Boolean,
    content: @Composable AnimatedVisibilityScope.(Screen) -> Unit
) {
    val slideTransitions = remember(slideAnimationSpec) {
        StyxSlideTransition.horizontal(slideAnimationSpec)
    }

    CurrentScreenPredictiveBack(
        navigator = navigator,
        defaultPredictiveBackTransition = IOSSlideTransitionPredictiveBack,
        enabled = enabled,
        defaultOnScreenAppearTransition = slideTransitions.appear,
        defaultOnScreenDisappearTransition = slideTransitions.disappear,
        content = content
    )
}
