package moe.styx.common.compose.components.misc

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.files.collectWithEmptyInitial
import moe.styx.common.compose.threads.Heartbeats
import moe.styx.common.data.ActiveUser
import moe.styx.common.data.Media
import moe.styx.common.data.MediaEntry
import moe.styx.common.extension.eqI
import moe.styx.common.extension.toBoolean

@Composable
fun OnlineUsersIcon(onClickMedia: (Media) -> Unit) {
    var showUserDropDown by remember { mutableStateOf(false) }
    val users by Heartbeats.currentUserState.collectAsState()
    val mediaList by Storage.stores.mediaStore.collectWithEmptyInitial()
    val entryList by Storage.stores.entryStore.collectWithEmptyInitial()
    val numUsers = users.distinctBy { it.user.GUID }.size

    UsersIconWithNum(numUsers) {
        showUserDropDown = if (showUserDropDown) false
        else numUsers > 0
    }
    DropdownMenu(showUserDropDown, { showUserDropDown = false }, Modifier.defaultMinSize(260.dp, 0.dp)) {
        Text("Online Users", Modifier.padding(7.dp, 10.dp), style = MaterialTheme.typography.titleLarge)
        HorizontalDivider(Modifier.fillMaxWidth().padding(10.dp, 0.dp, 10.dp, 8.dp), thickness = 3.dp)
        UserListComponent(users, mediaList, entryList, onClickMedia)
    }
}

@Composable
fun UsersIconWithNum(num: Int, onClick: () -> Unit) {
    BadgedBox(badge = {
        Badge(Modifier.size(20.dp).offset((-10).dp, 6.dp), MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.onSecondary) {
            Text("$num")
        }
    }) {
        IconButton(onClick = onClick, content = { Icon(Icons.Filled.Group, "Online userlist") })
    }
}

@Composable
fun UserListComponent(userList: List<ActiveUser>, mediaList: List<Media>, entryList: List<MediaEntry>, onClickMedia: (Media) -> Unit) {
    userList.forEachIndexed { index, user ->
        if (index != 0)
            HorizontalDivider(Modifier.fillMaxWidth().padding(10.dp, 10.dp), thickness = 1.dp)

        Row(Modifier.padding(10.dp, if (index != 0) 0.dp else 5.dp, 0.dp, 5.dp), verticalAlignment = Alignment.CenterVertically) {
            when (user.deviceType) {
                "PC" -> Icon(Icons.Filled.Computer, "PC")
                "Laptop" -> Icon(Icons.Filled.LaptopWindows, "Laptop")
                "Phone" -> Icon(Icons.Filled.PhoneAndroid, "Phone")
                "Tablet" -> Icon(Icons.Filled.Tablet, "Tablet")
                else -> Icon(Icons.Filled.DeviceUnknown, "Unknown")
            }
            Column(Modifier.padding(10.dp, 0.dp)) {
                Text(user.user.name, Modifier.padding(3.dp, 0.dp, 0.dp, 0.dp), style = MaterialTheme.typography.titleMedium)
                if (user.mediaActivity != null) {
                    val entry = entryList.find { it.GUID eqI user.mediaActivity?.mediaEntry }
                    val parentMedia = mediaList.find { it.GUID eqI entry?.mediaID }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (user.mediaActivity!!.playing)
                            Icon(Icons.Filled.PlayArrow, "Playing", Modifier.size(30.dp).padding(0.dp, 0.dp, 10.dp, 0.dp))
                        else
                            Icon(Icons.Filled.Pause, "Paused", Modifier.size(30.dp).padding(0.dp, 0.dp, 10.dp, 0.dp))
                        if (entry == null || parentMedia == null)
                            Text("Unknown", style = MaterialTheme.typography.bodyMedium)
                        else
                            Text(
                                "${parentMedia.name}${if (parentMedia.isSeries.toBoolean()) " - ${entry.entryNumber}" else ""}",
                                Modifier.clickable {
                                    onClickMedia(parentMedia)
                                }, style = MaterialTheme.typography.bodyMedium
                            )
                    }
                }
            }
        }
    }
}