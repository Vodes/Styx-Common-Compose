package moe.styx.common.compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope
import moe.styx.common.compose.utils.LocalGlobalNavigator

@Composable
fun Screen.LaunchedEffectWhenCurrentScreen(
    vararg keys: Any?,
    block: suspend CoroutineScope.() -> Unit
) {
    val nav = LocalGlobalNavigator.current

    LaunchedEffect(nav.current.key, *keys) {
        if (nav.current.key == key) {
            block()
        }
    }
}
