package moe.styx.common.compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.painter.Painter
import io.github.hristogochev.vortex.tab.CurrentTabNoTransitions as VortexCurrentTabNoTransitions
import io.github.hristogochev.vortex.tab.Tab as VortexTab

data class TabOptions(
    val index: UInt,
    val title: String,
    val icon: Painter? = null
)

interface Tab : VortexTab {
    val options: TabOptions
        @Composable get

    override val index: UInt
        get() = 0u
}

val LocalTabNavigator: ProvidableCompositionLocal<TabNavigator> =
    staticCompositionLocalOf { error("TabNavigator not initialized") }

@Composable
fun TabNavigator(
    tab: Tab,
    content: @Composable (tabNavigator: TabNavigator) -> Unit = { CurrentTab() }
) {
    Navigator(tab) { navigator ->
        val tabNavigator = remember(navigator) { TabNavigator(navigator) }

        CompositionLocalProvider(LocalTabNavigator provides tabNavigator) {
            content(tabNavigator)
        }
    }
}

class TabNavigator internal constructor(
    internal val navigator: Navigator
) {
    var current: Tab
        get() = navigator.current as Tab
        set(tab) {
            navigator.current = tab
        }
}

@Composable
fun CurrentTab() {
    val tabNavigator = LocalTabNavigator.current

    VortexCurrentTabNoTransitions(tabNavigator.navigator)
}
