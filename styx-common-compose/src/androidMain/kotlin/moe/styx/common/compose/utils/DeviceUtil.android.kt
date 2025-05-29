package moe.styx.common.compose.utils

import android.content.Intent
import android.net.Uri
import android.os.Build
import com.russhwolf.settings.get
import moe.styx.common.compose.AppContextImpl
import moe.styx.common.compose.AppContextImpl.appConfig
import moe.styx.common.compose.settings
import moe.styx.common.data.DeviceInfo
import moe.styx.common.util.Log

actual fun fetchDeviceInfo(): DeviceInfo {
    val supportedABIs = Build.SUPPORTED_ABIS.joinToString()
    val supportSOC = Build.VERSION.SDK_INT >= 31
    return DeviceInfo(
        if (settings["is-tablet", false]) "Tablet" else "Phone",
        "",
        "${Build.MANUFACTURER}/${Build.BRAND} ${Build.MODEL}",
        if (supportSOC) "${Build.SOC_MANUFACTURER} ${Build.SOC_MODEL} ($supportedABIs)" else "Unknown ($supportedABIs)",
        null,
        "Android",
        Build.VERSION.RELEASE,
        null,
        null,
        appConfig().appSecret
    )
}

actual fun openURI(uri: String?) {
    val uri = uri?.let { uriString ->
        runCatching { Uri.parse(uriString) }
            .onFailure { Log.e(exception = it) { "Failed to parse URI: $uri" } }
            .getOrNull()
    } ?: return
    val intent = Intent().apply {
        action = Intent.ACTION_VIEW
        data = uri
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    AppContextImpl.get().startActivity(intent)
}