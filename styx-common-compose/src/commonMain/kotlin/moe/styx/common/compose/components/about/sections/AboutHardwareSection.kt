package moe.styx.common.compose.components.about.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.styx.common.compose.components.about.BasicChip
import moe.styx.common.data.DeviceInfo

@Composable
internal fun HardwareSection(deviceInfo: DeviceInfo) {
    val chipColor = MaterialTheme.colorScheme.surfaceColorAtElevation(20.dp)
    Text("Hardware", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(5.dp))
    Column {
        if (!deviceInfo.model.isNullOrBlank())
            BasicChip("Model: ${deviceInfo.model}", icon = Icons.Default.Style, "Model", chipColor)
        BasicChip("CPU: ${deviceInfo.cpu}", icon = Icons.Default.Memory, "CPU", chipColor)
        if (!deviceInfo.gpu.isNullOrBlank())
            BasicChip("GPU: ${deviceInfo.gpu}", icon = Icons.Default.Brush, "GPU", chipColor)
    }
}