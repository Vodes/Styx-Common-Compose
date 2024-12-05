package moe.styx.common.compose.utils

import android.os.Build
import com.russhwolf.settings.get
import moe.styx.common.compose.AppContextImpl.appConfig
import moe.styx.common.compose.settings
import moe.styx.common.data.DeviceInfo

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