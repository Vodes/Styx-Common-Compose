package moe.styx.common.compose.navigation

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import io.github.hristogochev.vortex.transitions.AndroidSlideTransitionTransitionPredictiveBack

@Composable
actual fun StyxCurrentScreenPredictiveBack(
    navigator: Navigator,
    slideAnimationSpec: FiniteAnimationSpec<IntOffset>,
    enabled: Boolean,
    content: @Composable (Screen) -> Unit
) {
    val context = LocalContext.current
    val usePredictiveBack = enabled && remember(context) {
        context.isLikelyUsingGestureNavigation()
    }
    val density = LocalDensity.current
    val slideTransitions = remember(slideAnimationSpec) {
        StyxSlideTransition.horizontal(slideAnimationSpec)
    }

    if (usePredictiveBack) {
        CurrentScreenPredictiveBack(
            navigator = navigator,
            defaultPredictiveBackTransition = AndroidSlideTransitionTransitionPredictiveBack(density),
            enabled = true,
            defaultOnScreenAppearTransition = slideTransitions.appear,
            defaultOnScreenDisappearTransition = slideTransitions.disappear,
            content = { content(it) }
        )
    } else {
        Box(
            modifier = Modifier.onKeyEvent { event ->
                if (event.key == Key.Back && event.type == KeyEventType.KeyUp && navigator.canPop && navigator.current.canPop) {
                    navigator.pop()
                    true
                } else {
                    false
                }
            }
        ) {
            CurrentScreen(
                navigator = navigator,
                defaultOnScreenAppearTransition = slideTransitions.appear,
                defaultOnScreenDisappearTransition = slideTransitions.disappear,
                content = { content(it) }
            )
        }
    }
}

private fun Context.isLikelyUsingGestureNavigation(): Boolean {
    if (packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)) {
        return false
    }

    return runCatching {
        Settings.Secure.getInt(contentResolver, "navigation_mode") == 2
    }.getOrDefault(true)
}
