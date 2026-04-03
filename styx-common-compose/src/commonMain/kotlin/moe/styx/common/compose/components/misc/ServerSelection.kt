package moe.styx.common.compose.components.misc

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import moe.styx.common.compose.components.misc.Toggles.settingsContainer
import moe.styx.common.compose.settings
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.compose.utils.LocalIsTv
import moe.styx.common.compose.viewmodels.MainDataViewModel
import moe.styx.common.extension.eqI

@Composable
fun ServerSelection() {
    val nav = LocalGlobalNavigator.current
    val sm = nav.rememberNavigatorScreenModel("main-vm") { MainDataViewModel() }
    val storage by sm.storageFlow.collectAsState()
    var selectedServerString by remember { mutableStateOf(settings["selected-server", ""]) }
    Text("Server Selector", Modifier.padding(10.dp, 5.dp), style = MaterialTheme.typography.titleLarge)
    Column(Modifier.settingsContainer()) {
        ServerItem("Main", "DE", selectedServerString.isBlank() || storage.proxyServerList.isEmpty()) {
            settings["selected-server"] = "".also { selectedServerString = it }
        }
        storage.proxyServerList.forEach { server ->
            ServerItem(server.name, server.country, selectedServerString eqI server.name) {
                settings["selected-server"] = server.name.also { selectedServerString = server.name }
            }
        }
    }
}

@Composable
fun ServerItem(name: String, country: String, isSelected: Boolean, onSelect: () -> Unit) {
    val isTv = LocalIsTv.current
    var isFocused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = Modifier.fillMaxWidth()
            .padding(6.dp, 4.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource, null) { onSelect() }
            .then(
                if (isTv) {
                    Modifier.border(
                        2.dp,
                        if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
                        moe.styx.common.compose.components.AppShapes.large
                    )
                } else {
                    Modifier
                }
            ),
        shape = moe.styx.common.compose.components.AppShapes.large,
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(
            if (isTv) {
                if (isFocused) 4.dp else 2.dp
            } else {
                1.dp
            }
        ),
        tonalElevation = if (isTv) {
            if (isFocused) 2.dp else 0.dp
        } else {
            1.dp
        },
        shadowElevation = if (isTv) 0.dp else 1.dp
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            RadioButton(
                isSelected,
                onClick = onSelect,
                modifier = Modifier.focusProperties { canFocus = !isTv }
            )
            Text(name, modifier = Modifier.weight(1f))
            Text(country, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
