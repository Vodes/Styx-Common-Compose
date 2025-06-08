package moe.styx.common.compose.components.about.sections

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BuildCircle
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import moe.styx.common.compose.AppConfig
import moe.styx.common.compose.components.about.BasicChip
import moe.styx.common.data.DeviceInfo

@Composable
internal fun AppSection(config: AppConfig, deviceInfo: DeviceInfo) {
    val chipColor = MaterialTheme.colorScheme.tertiaryContainer.copy(0.7f)
    Row(Modifier.fillMaxWidth()) {
        BasicChip("Version: ${config.appVersion}", icon = Icons.Default.BuildCircle, "Version", chipColor)
        val deviceString = deviceInfo.osVersion?.let { if (it.isBlank()) null else "${deviceInfo.os} ($it)" } ?: deviceInfo.os
        BasicChip(
            "OS: $deviceString",
            icon = when (deviceInfo.type) {
                "PC" -> Icons.Outlined.DesktopWindows
                "Laptop" -> Icons.Outlined.LaptopChromebook
                "Phone" -> Icons.Outlined.PhoneAndroid
                "Tablet" -> Icons.Outlined.Tablet
                else -> Icons.Outlined.DeviceUnknown
            },
            "Device",
            chipColor,
        )
    }
    if (!deviceInfo.jvm.isNullOrBlank()) {
        val jvmString = deviceInfo.jvm?.let {
            if (!deviceInfo.jvmVersion.isNullOrBlank() && deviceInfo.jvm?.contains(deviceInfo.jvmVersion!!) == false)
                "$it (${deviceInfo.jvmVersion}"
            it
        }
        BasicChip("JVM: $jvmString", icon = Icons.Default.Coffee, "Java VM", chipColor)
    }
}