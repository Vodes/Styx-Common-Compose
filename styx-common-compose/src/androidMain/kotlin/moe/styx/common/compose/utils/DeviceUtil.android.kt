package moe.styx.common.compose.utils

import android.os.Build
import moe.styx.common.compose.appConfig
import moe.styx.common.data.DeviceInfo

actual fun fetchDeviceInfo(): DeviceInfo {
    println(Build())
    return DeviceInfo(
        "Phone",
        Build.DEVICE,
        Build.MODEL,
        Build.SUPPORTED_ABIS.joinToString(),
        null,
        "Android",
        null,
        null,
        null,
        appConfig().appSecret
    )
}