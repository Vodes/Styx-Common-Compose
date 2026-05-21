package moe.styx.common.compose.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import moe.styx.common.compose.navigation.Tab
import moe.styx.common.compose.navigation.TabOptions

@Composable
fun createTabOptions(title: String, icon: ImageVector, index: UInt = 0u): TabOptions {
    val iconPainter = rememberVectorPainter(icon)

    return remember(index) {
        TabOptions(
            index = index,
            title = title,
            icon = iconPainter
        )
    }
}

abstract class SimpleTab(val name: String, val icon: ImageVector, override val index: UInt = 0u) : Tab {
    override val options: TabOptions
        @Composable
        get() = createTabOptions(name, icon, index)
}
