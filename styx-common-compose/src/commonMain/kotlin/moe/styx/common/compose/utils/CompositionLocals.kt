package moe.styx.common.compose.utils

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import cafe.adriel.voyager.navigator.Navigator
import com.dokar.sonner.ToasterState

val LocalGlobalNavigator: ProvidableCompositionLocal<Navigator> = staticCompositionLocalOf { error("LocalGlobalNavigator not initialized") }

val LocalToaster: ProvidableCompositionLocal<ToasterState> = staticCompositionLocalOf { error("LocalToaster not initialized") }