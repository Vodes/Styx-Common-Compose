package moe.styx.common.compose.utils

import com.russhwolf.settings.get
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import moe.styx.common.compose.AppContextImpl.appConfig
import moe.styx.common.compose.settings
import moe.styx.common.data.DeviceInfo
import moe.styx.common.util.Log
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.posix.uname
import platform.posix.utsname

actual fun fetchDeviceInfo(): DeviceInfo {
    val machine = deviceMachineIdentifier()

    return DeviceInfo(
        if (settings["is-tablet", false]) "Tablet" else "Phone",
        UIDevice.currentDevice.name,
        UIDevice.currentDevice.model,
        machine?.let { "Apple Silicon ($it)" } ?: "Apple Silicon",
        null,
        UIDevice.currentDevice.systemName,
        UIDevice.currentDevice.systemVersion,
        null,
        null,
        appConfig().appSecret
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun deviceMachineIdentifier(): String? = memScoped {
    val systemInfo = alloc<utsname>()
    if (uname(systemInfo.ptr) != 0) return null
    systemInfo.machine.toKString().takeIf { it.isNotBlank() }
}

actual fun openURI(uri: String?) {
    if (uri.isNullOrBlank())
        return

    val url = NSURL.URLWithString(uri)
    if (url == null) {
        Log.e { "Failed to parse URI: $uri" }
        return
    }

    UIApplication.sharedApplication.openURL(
        url = url,
        options = emptyMap<Any?, Any>(),
        completionHandler = { success ->
            if (!success) {
                Log.e { "Failed to open URI: $uri" }
            }
        }
    )
}
