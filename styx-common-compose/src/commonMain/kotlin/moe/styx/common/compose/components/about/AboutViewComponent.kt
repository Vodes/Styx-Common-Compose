package moe.styx.common.compose.components.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Copyright
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import moe.styx.common.Platform
import moe.styx.common.compose.AppContextImpl
import moe.styx.common.compose.components.about.sections.*
import moe.styx.common.compose.components.misc.ExpandableSettings
import moe.styx.common.compose.components.tracking.common.ElevatedSurface
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.compose.utils.fetchDeviceInfo
import moe.styx.common.compose.utils.openURI
import moe.styx.common.compose.viewmodels.MainDataViewModel
import moe.styx.styx_common_compose.generated.resources.Res
import moe.styx.styx_common_compose.generated.resources.icon
import org.jetbrains.compose.resources.painterResource

@Composable
fun AboutViewComponent(appName: String) {
    val nav = LocalGlobalNavigator.current
    val scrollState = rememberScrollState()
    val mainVm = nav.rememberNavigatorScreenModel("main-vm") { MainDataViewModel() }
    val storage by mainVm.storageFlow.collectAsState()
    var isAppInfoExpanded by remember { mutableStateOf(true) }
    var isLibrariesExpanded by remember { mutableStateOf(false) }
    var isPlayerExpanded by remember { mutableStateOf(false) }
    Column(
        Modifier.padding(10.dp).fillMaxWidth(1F).verticalScroll(scrollState, true),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedCard {
            Column(Modifier.widthIn(0.dp, 550.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(appName, style = MaterialTheme.typography.displayMedium, modifier = Modifier.padding(10.dp))
                val appIconPainter = painterResource(Res.drawable.icon)
                Image(appIconPainter, "$appName icon", modifier = Modifier.padding(14.dp).widthIn(0.dp, 160.dp))
                Text(
                    "A multiplatform client for yet another open-source mediaserver stack",
                    Modifier.padding(28.dp, 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )
                GitAuthorsChips()
                Spacer(Modifier.height(5.dp))
            }
        }
        Spacer(Modifier.height(25.dp))
        val config = AppContextImpl.appConfig()
        val deviceInfo = fetchDeviceInfo()
        ExpandableSettings("App & System Info", isAppInfoExpanded, {
            isAppInfoExpanded = !isAppInfoExpanded
            if (isAppInfoExpanded) isLibrariesExpanded = false.also { isPlayerExpanded = false }
        }) {
            AppSection(config, deviceInfo)
            StatsSection(storage)
            HardwareSection(deviceInfo)
        }
        ExpandableSettings("Libraries", isLibrariesExpanded, {
            isLibrariesExpanded = !isLibrariesExpanded
            if (isLibrariesExpanded) isAppInfoExpanded = false.also { isPlayerExpanded = false }
        }) {
            LibrariesSection()
        }
        ExpandableSettings("Player", isPlayerExpanded, {
            isPlayerExpanded = !isPlayerExpanded
            if (isPlayerExpanded) isLibrariesExpanded = false.also { isAppInfoExpanded = false }
        }) {
            if (Platform.current == Platform.JVM)
                DesktopPlayerSection()
            else if (Platform.current == Platform.ANDROID)
                AndroidPlayerSection()

            TrackingPlayerSection()
        }
    }
}

@Composable
internal fun LibRow(title: String, description: String, repoURL: String) {
    Row(Modifier.padding(2.dp, 0.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        BasicChip(
            title,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier.widthIn(120.dp, Dp.Unspecified),
            textWidth = 105.dp
        ) {
            openURI(repoURL)
        }
        Text("-", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(4.dp, 0.dp))
        Text(description, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
internal fun LibCard(title: String, description: String, licenseText: String, repoURL: String, authors: String) {
    ElevatedSurface(Modifier.padding(2.dp, 6.dp).fillMaxWidth()) {
        Column(Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(5.dp, 0.dp))
                Spacer(Modifier.weight(1f))
                BasicChip(licenseText, Icons.Default.Copyright, "License", color = MaterialTheme.colorScheme.tertiaryContainer) {
                    openURI("https://spdx.org/licenses/$licenseText")
                }
            }
            HorizontalDivider(Modifier.padding(4.dp, 0.dp, 4.dp, 8.dp).fillMaxWidth(), thickness = 2.dp)
            Text(description, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(4.dp, 0.dp))
            HorizontalDivider(Modifier.padding(4.dp, 8.dp, 4.dp, 1.dp).fillMaxWidth(), thickness = 2.dp)
            Row(Modifier.padding(0.dp, 4.dp, 0.dp, 0.dp), verticalAlignment = Alignment.CenterVertically) {
                BasicChip("Source Code", Icons.Default.Code, "Go to Git Repo", color = MaterialTheme.colorScheme.tertiaryContainer) {
                    openURI(repoURL)
                }
                Spacer(Modifier.weight(1f))
                BasicChip(authors, Icons.Default.Engineering, "Authors", color = MaterialTheme.colorScheme.secondaryContainer)
            }
        }
    }
}

@Composable
internal fun GitAuthorsChips() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        BasicChip("Source Code", Icons.Default.Code, "Go to Github Repository", color = MaterialTheme.colorScheme.tertiaryContainer) {
            openURI(if (Platform.current == Platform.JVM) "https://github.com/Vodes/Styx-2" else "https://github.com/Vodes/Styx-2m")
        }
        Spacer(Modifier.width(3.dp))
        BasicChip("Vodes & Contributors", Icons.Default.Copyright, "Authors", MaterialTheme.colorScheme.secondaryContainer.copy(0.7F))
    }
}