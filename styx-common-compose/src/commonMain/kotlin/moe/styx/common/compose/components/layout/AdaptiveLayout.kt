package moe.styx.common.compose.components.layout

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
inline fun AdaptiveLayout(
    modifier: Modifier = Modifier,
    crossinline columnContent: @Composable ColumnScope.() -> Unit = {},
    crossinline rowContent: @Composable RowScope.() -> Unit = {}
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth < 400.dp) {
            Column(modifier) {
                columnContent(this)
            }
        } else {
            Row(modifier) {
                rowContent(this)
            }
        }
    }
}