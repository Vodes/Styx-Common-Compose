package moe.styx.common.compose.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

@Composable
fun createTabOptions(title: String, icon: ImageVector): TabOptions {
    val iconPainter = rememberVectorPainter(icon)

    return remember {
        TabOptions(
            index = 0u,
            title = title,
            icon = iconPainter
        )
    }
}

abstract class SimpleTab(val name: String, val icon: ImageVector) : Tab {
    override val options: TabOptions
        @Composable
        get() = createTabOptions(name, icon)
}