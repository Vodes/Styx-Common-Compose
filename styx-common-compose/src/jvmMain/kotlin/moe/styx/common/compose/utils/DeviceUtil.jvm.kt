package moe.styx.common.compose.utils

import moe.styx.common.compose.AppContextImpl.appConfig
import moe.styx.common.data.DeviceInfo
import moe.styx.common.extension.eqI
import moe.styx.common.isWindows
import moe.styx.common.util.Log
import oshi.SystemInfo
import java.awt.Desktop
import java.io.File
import java.net.URI

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

actual fun openURI(uri: String?) {
    if (uri.isNullOrBlank())
        return

    val uri = runCatching {
        URI(uri)
    }.onFailure {
        Log.e(exception = it) { "Failed to parse URI: $uri" }
    }.getOrNull() ?: return

    openURI(uri)
}

fun openURI(uri: URI) {
    if (!isWindows) {
        val xdgOpen = getExecutableFromPath("xdg-open")
        if (xdgOpen != null) {
            val result = ProcessBuilder(listOf(xdgOpen.absolutePath, uri.toString())).start().waitFor()
            if (result == 0)
                return
        }
    }
    if (Desktop.isDesktopSupported()) {
        runCatching { Desktop.getDesktop().browse(uri) }.onFailure { Log.e(exception = it) { "Failed to open URI: $uri" } }
    }
}

fun getExecutableFromPath(name: String): File? {
    var name = name
    if (isWindows && !name.contains(".exe"))
        name = "$name.exe"
    val pathDirs = System.getenv("PATH").split(File.pathSeparator)
        .map { File(it) }.filter { it.exists() && it.isDirectory }

    return pathDirs.flatMap { it.listFiles()?.asList() ?: listOf() }.find { (if (isWindows) it.name else it.nameWithoutExtension) eqI name }
}