package moe.styx.common.compose.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntOffset

val DefaultStyxSlideAnimationSpec: FiniteAnimationSpec<IntOffset> = spring(
    stiffness = Spring.StiffnessMediumLow * 2f,
    visibilityThreshold = IntOffset.VisibilityThreshold
)

val DefaultStyxPredictiveBackSlideAnimationSpec: FiniteAnimationSpec<IntOffset> = spring(
    stiffness = Spring.StiffnessMediumLow * 2.5f,
    visibilityThreshold = IntOffset.VisibilityThreshold
)

data class StyxScreenTransitions(
    val appear: ScreenTransition,
    val disappear: ScreenTransition
)

object StyxSlideTransition {
    fun horizontal(
        animationSpec: FiniteAnimationSpec<IntOffset> = DefaultStyxSlideAnimationSpec
    ): StyxScreenTransitions {
        return StyxScreenTransitions(
            appear = HorizontalAppear(animationSpec),
            disappear = HorizontalDisappear(animationSpec)
        )
    }

    private class HorizontalAppear(
        private val animationSpec: FiniteAnimationSpec<IntOffset>
    ) : ScreenTransition {
        override fun enter(): EnterTransition =
            slideInHorizontally(animationSpec) { size -> size }

        override fun exit(): ExitTransition =
            slideOutHorizontally(animationSpec) { size -> -size }
    }

    private class HorizontalDisappear(
        private val animationSpec: FiniteAnimationSpec<IntOffset>
    ) : ScreenTransition {
        override fun enter(): EnterTransition =
            slideInHorizontally(animationSpec) { size -> -size }

        override fun exit(): ExitTransition =
            slideOutHorizontally(animationSpec) { size -> size }
    }
}

@Composable
fun StyxCurrentScreen(
    navigator: Navigator,
    slideAnimationSpec: FiniteAnimationSpec<IntOffset> = DefaultStyxSlideAnimationSpec,
    content: @Composable AnimatedVisibilityScope.(Screen) -> Unit = { it.Content() }
) {
    val transitions = remember(slideAnimationSpec) {
        StyxSlideTransition.horizontal(slideAnimationSpec)
    }

    CurrentScreen(
        navigator = navigator,
        defaultOnScreenAppearTransition = transitions.appear,
        defaultOnScreenDisappearTransition = transitions.disappear,
        content = content
    )
}

@Composable
expect fun StyxCurrentScreenPredictiveBack(
    navigator: Navigator,
    slideAnimationSpec: FiniteAnimationSpec<IntOffset> = DefaultStyxPredictiveBackSlideAnimationSpec,
    enabled: Boolean = true,
    content: @Composable (Screen) -> Unit = { it.Content() }
)
