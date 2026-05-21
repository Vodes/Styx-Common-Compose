package moe.styx.common.compose.utils

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import com.dokar.sonner.ToasterState
import moe.styx.common.compose.navigation.Navigator

val LocalGlobalNavigator: ProvidableCompositionLocal<Navigator> = staticCompositionLocalOf { error("LocalGlobalNavigator not initialized") }

val LocalToaster: ProvidableCompositionLocal<ToasterState> = staticCompositionLocalOf { error("LocalToaster not initialized") }

val LocalIsTv: ProvidableCompositionLocal<Boolean> = staticCompositionLocalOf { false }
