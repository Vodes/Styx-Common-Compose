package moe.styx.common.compose.utils

import moe.styx.common.compose.appConfig
import moe.styx.common.data.DeviceInfo
import platform.UIKit.UIDevice

actual fun fetchDeviceInfo(): DeviceInfo {
    return DeviceInfo(
        "Phone",
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