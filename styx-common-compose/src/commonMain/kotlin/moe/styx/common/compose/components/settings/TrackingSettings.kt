package moe.styx.common.compose.components.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import moe.styx.common.compose.utils.LocalIsTv
import moe.styx.common.compose.utils.openURI
import moe.styx.common.compose.viewmodels.MainDataViewModel

@Composable
fun TrackingSettings() {
    val nav = LocalGlobalNavigator.current
    val sm = nav.rememberNavigatorScreenModel("main-vm") { MainDataViewModel() }
    val isTv = LocalIsTv.current
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
                TrackingConnectionIndicator(
                    username = sm.anilistUser?.name,
                    siteName = "AniList",
                    userBaseURL = "https://anilist.co/user",
                    clickable = !isTv
                )
                TrackingConnectionIndicator(
                    username = sm.malUser?.name,
                    siteName = "MyAnimeList",
                    userBaseURL = "https://myanimelist.net/profile",
                    clickable = !isTv
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "To dis-/connect an account, please go to the website and check the tabs in the user section.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(6.dp, 4.dp)
            )
            if (isTv) {
                Column(Modifier.padding(0.dp, 2.dp)) {
                    TvTrackingActionButton(
                        text = "Refresh",
                        onClick = { sm.reauthorizeStyx() },
                        modifier = Modifier.padding(6.dp, 6.dp, 6.dp, 0.dp),
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        icon = { Icon(Icons.Outlined.Refresh, "Refresh tracking connections") }
                    )
                }
            } else {
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
        }
        Spacer(Modifier.height(2.dp))
    }
}

@Composable
private fun TvTrackingActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onPrimary,
    icon: @Composable (() -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    val buttonBorderColor = if (isFocused) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)
    }
    val buttonBorderWidth = if (isFocused) 3.dp else 1.dp
    val resolvedContainerColor = if (!isFocused) {
        Color(
            red = (containerColor.red * 0.82f) + (MaterialTheme.colorScheme.surface.red * 0.18f),
            green = (containerColor.green * 0.82f) + (MaterialTheme.colorScheme.surface.green * 0.18f),
            blue = (containerColor.blue * 0.82f) + (MaterialTheme.colorScheme.surface.blue * 0.18f),
            alpha = 1f
        )
    } else {
        containerColor
    }
    val resolvedContentColor = if (!isFocused) contentColor.copy(alpha = 0.92f) else contentColor

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                buttonBorderWidth,
                buttonBorderColor,
                AppShapes.medium
            )
            .heightIn(min = 48.dp),
        shape = AppShapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = resolvedContainerColor,
            contentColor = resolvedContentColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, focusedElevation = 0.dp, pressedElevation = 0.dp)
    ) {
        icon?.invoke()
        if (icon != null) {
            Spacer(Modifier.width(2.dp))
        }
        Text(text, fontWeight = if (isFocused) FontWeight.SemiBold else FontWeight.Medium)
    }
}
