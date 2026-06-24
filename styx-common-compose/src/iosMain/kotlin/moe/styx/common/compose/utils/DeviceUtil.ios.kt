package moe.styx.common.compose.utils

import com.russhwolf.settings.get
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ULongVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import moe.styx.common.compose.AppContextImpl.appConfig
import moe.styx.common.compose.settings
import moe.styx.common.data.DeviceInfo
import moe.styx.common.util.Log
import platform.Foundation.NSURL
import platform.darwin.sysctlbyname
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice

actual fun fetchDeviceInfo(): DeviceInfo {
    val machine = sysctlString("hw.machine")

    return DeviceInfo(
        if (settings["is-tablet", false]) "Tablet" else "Phone",
        UIDevice.currentDevice.name,
        UIDevice.currentDevice.model,
        sysctlString("machdep.cpu.brand_string") ?: machine?.let { "Apple Silicon ($it)" },
        null,
        UIDevice.currentDevice.systemName,
        UIDevice.currentDevice.systemVersion,
        null,
        null,
        appConfig().appSecret
    ).also { Log.d { it.toString() } }
}

@OptIn(ExperimentalForeignApi::class)
private fun sysctlString(name: String): String? = memScoped {
    val size = alloc<ULongVar>()
    if (sysctlbyname(name, null, size.ptr, null, 0.convert()) != 0) return null

    val buffer = allocArray<ByteVar>(size.value.toInt())
    if (sysctlbyname(name, buffer, size.ptr, null, 0.convert()) != 0) return null

    buffer.toKString().takeIf { it.isNotBlank() }
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
