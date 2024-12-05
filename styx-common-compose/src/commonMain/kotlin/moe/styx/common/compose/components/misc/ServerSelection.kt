package moe.styx.common.compose.components.misc

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import moe.styx.common.compose.components.misc.Toggles.settingsContainer
import moe.styx.common.compose.settings
import moe.styx.common.compose.utils.LocalGlobalNavigator
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
    ListItem(headlineContent = {
        Text(name)
    }, leadingContent = {
        RadioButton(isSelected, onClick = onSelect)
    }, trailingContent = {
        Text(country)
    }, modifier = Modifier.clickable { onSelect() })
}
