package moe.styx.common.compose.components.buttons

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
actual fun ToolTipWrapper(text: String, modifier: Modifier, content: @Composable () -> Unit) {
    TooltipArea(delayMillis = 450, tooltip = {
        ElevatedCard(elevation = CardDefaults.elevatedCardElevation(5.dp)) {
            Text(text, modifier = modifier.padding(5.dp))
        }
    }, content = content)
}