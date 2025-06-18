package moe.styx.common.compose.utils

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
actual fun fetchWindowSize(): LayoutSizes {
    val config = LocalConfiguration.current
    return LayoutSizes(config.screenWidthDp, config.screenHeightDp)
}