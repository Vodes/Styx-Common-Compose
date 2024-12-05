package moe.styx.common.compose.utils

import com.russhwolf.settings.get
import moe.styx.common.compose.AppContextImpl.appConfig
import moe.styx.common.compose.settings
import moe.styx.common.data.DeviceInfo
import platform.UIKit.UIDevice

actual fun fetchDeviceInfo(): DeviceInfo {
    return DeviceInfo(
        if (settings["is-tablet", false]) "Tablet" else "Phone",
        UIDevice.currentDevice.systemName(),
        null,
        null,
        null,
        "iOS",
        UIDevice.currentDevice.systemVersion,
        null,
        null,
        appConfig().appSecret
    )
}