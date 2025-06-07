package moe.styx.common.compose.components.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import moe.styx.common.compose.AppContextImpl.appConfig
import moe.styx.common.compose.components.AppShapes
import moe.styx.common.compose.components.misc.Toggles
import moe.styx.common.compose.components.tracking.common.TrackingConnectionIndicator
import moe.styx.common.compose.settings
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.compose.utils.openURI
import moe.styx.common.compose.viewmodels.MainDataViewModel

@Composable
fun TrackingSettings() {
    val nav = LocalGlobalNavigator.current
    val sm = nav.rememberNavigatorScreenModel("main-vm") { MainDataViewModel() }
    Column(Modifier.fillMaxWidth()) {
        Toggles.ContainerSwitch(
            "Auto-Sync",
            "Automatically update remote list status after watching something.",
            value = settings["auto-sync", false]
        ) {
            settings["auto-sync"] = it
        }
        Column(Modifier.fillMaxWidth().padding(12.dp, 5.dp)) {
            Row {
                TrackingConnectionIndicator(username = sm.anilistUser?.name, siteName = "AniList", userBaseURL = "https://anilist.co/user")
                TrackingConnectionIndicator(username = sm.malUser?.name, siteName = "MyAnimeList", userBaseURL = "https://myanimelist.net/profile")
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "To dis-/connect an account, please go to the website and check the tabs in the user section.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(6.dp, 4.dp)
            )
            Row(Modifier.padding(0.dp, 2.dp)) {
                val appConfig = appConfig()
                Button({
                    openURI("${appConfig.siteURL}/user")
                }, shape = AppShapes.medium, modifier = Modifier.padding(6.dp, 0.dp)) {
                    Icon(Icons.Outlined.OpenInBrowser, "Open styx website")
                    Spacer(Modifier.width(5.dp))
                    Text("Open ${appConfig.site}")
                }
                Button(
                    { sm.reauthorizeStyx() },
                    shape = AppShapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                ) {
                    Icon(Icons.Outlined.Refresh, "Refresh tracking connections")
                    Spacer(Modifier.width(2.dp))
                    Text("Refresh")
                }
            }
        }
        Spacer(Modifier.height(2.dp))
    }
}