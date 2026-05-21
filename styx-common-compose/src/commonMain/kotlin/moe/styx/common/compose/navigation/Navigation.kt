package moe.styx.common.compose.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.ProvidableCompositionLocal
import io.github.hristogochev.vortex.model.rememberNavigatorScreenModel as vortexRememberNavigatorScreenModel
import io.github.hristogochev.vortex.model.rememberScreenModel as vortexRememberScreenModel
import io.github.hristogochev.vortex.model.screenModelScope as vortexScreenModelScope
import io.github.hristogochev.vortex.navigator.LocalNavigator as VortexLocalNavigator
import io.github.hristogochev.vortex.navigator.Navigator as VortexNavigator
import io.github.hristogochev.vortex.screen.CurrentScreen as VortexCurrentScreen
import io.github.hristogochev.vortex.screen.CurrentScreenPredictiveBack as VortexCurrentScreenPredictiveBack
import io.github.hristogochev.vortex.screen.Screen as VortexScreen
import kotlinx.coroutines.CoroutineScope

typealias Navigator = VortexNavigator
typealias Screen = VortexScreen
typealias ScreenKey = String
typealias ScreenModel = io.github.hristogochev.vortex.model.ScreenModel
typealias ScreenTransition = io.github.hristogochev.vortex.screen.ScreenTransition
typealias ScreenTransitionPredictiveBack = io.github.hristogochev.vortex.screen.ScreenTransitionPredictiveBack

val LocalNavigator: ProvidableCompositionLocal<Navigator?>
    get() = VortexLocalNavigator

val ScreenModel.screenModelScope: CoroutineScope
    get() = vortexScreenModelScope

@Composable
inline fun <reified T : ScreenModel> rememberScreenModel(
    tag: String? = null,
    crossinline factory: @DisallowComposableCalls () -> T
): T {
    return vortexRememberScreenModel(tag) { factory() }
}

@Composable
inline fun <reified T : ScreenModel> Navigator.rememberNavigatorScreenModel(
    tag: String? = null,
    crossinline factory: @DisallowComposableCalls () -> T
): T {
    return vortexRememberNavigatorScreenModel(tag) { factory() }
}

@Composable
fun Navigator(
    screen: Screen,
    disposeOnForgotten: Boolean = false,
    content: @Composable (navigator: Navigator) -> Unit = { CurrentScreen(it) }
) {
    VortexNavigator(screen, disposeOnForgotten, content)
}

@Composable
fun Navigator(
    screens: List<Screen>,
    disposeOnForgotten: Boolean = false,
    content: @Composable (navigator: Navigator) -> Unit = { CurrentScreen(it) }
) {
    VortexNavigator(screens, disposeOnForgotten, content)
}

@Composable
fun CurrentScreen(
    navigator: Navigator,
    defaultOnScreenAppearTransition: ScreenTransition? = null,
    defaultOnScreenDisappearTransition: ScreenTransition? = null,
    content: @Composable AnimatedVisibilityScope.(Screen) -> Unit = { it.Content() }
) {
    VortexCurrentScreen(
        navigator = navigator,
        defaultOnScreenAppearTransition = defaultOnScreenAppearTransition,
        defaultOnScreenDisappearTransition = defaultOnScreenDisappearTransition,
        content = content
    )
}

@Composable
fun CurrentScreenPredictiveBack(
    navigator: Navigator,
    defaultPredictiveBackTransition: ScreenTransitionPredictiveBack,
    enabled: Boolean = true,
    defaultOnScreenAppearTransition: ScreenTransition? = null,
    defaultOnScreenDisappearTransition: ScreenTransition? = null,
    content: @Composable AnimatedVisibilityScope.(Screen) -> Unit = { it.Content() }
) {
    VortexCurrentScreenPredictiveBack(
        navigator = navigator,
        defaultPredictiveBackTransition = defaultPredictiveBackTransition,
        enabled = enabled,
        defaultOnScreenAppearTransition = defaultOnScreenAppearTransition,
        defaultOnScreenDisappearTransition = defaultOnScreenDisappearTransition,
        content = content
    )
}
