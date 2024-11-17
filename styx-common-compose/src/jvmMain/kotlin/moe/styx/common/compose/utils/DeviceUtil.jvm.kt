package moe.styx.common.compose.utils

import moe.styx.common.compose.AppContextImpl.appConfig
import moe.styx.common.data.DeviceInfo
import oshi.SystemInfo

actual fun fetchDeviceInfo(): DeviceInfo {
    val sInfo = SystemInfo()
    val hal = sInfo.hardware
    val power = sInfo.hardware.powerSources
    val gpus = hal.graphicsCards
    val os = sInfo.operatingSystem

    return DeviceInfo(
        power?.firstOrNull()?.let { if (it.voltage == -1.0 || it.amperage == 0.0 || it.cycleCount == -1) "PC" else "Laptop" } ?: "PC",
        os.networkParams.hostName,
        null,
        hal.processor?.processorIdentifier?.name?.trim(),
        if (gpus.isEmpty()) null else gpus.joinToString { it.name },
        "$os".trim(),
        null,
        "${System.getProperty("java.vm.name")} (${System.getProperty("java.vm.version")})",
        System.getProperty("java.version"),
        appConfig().appSecret
    )
}