package moe.styx.common.compose.components.buttons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.datetime.Clock
import moe.styx.common.compose.utils.LocalGlobalNavigator

internal var lastPop = 0L

@Composable
fun PopButton(nav: Navigator? = null) {
    var navigator = nav
    if (nav == null)
        navigator = LocalGlobalNavigator.current
    IconButton(onClick = {
        val now = Clock.System.now().toEpochMilliseconds()
        val allowPop = (now - lastPop) > 500
        if (navigator!!.canPop && allowPop) {
            navigator.pop()
            lastPop = now
        }
    }, content = { Icon(Icons.Filled.Close, null) })
}