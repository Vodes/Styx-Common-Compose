package moe.styx.common.compose.components.about.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.styx.common.compose.components.about.BasicChip
import moe.styx.common.compose.components.about.BasicChipPainter
import moe.styx.common.compose.utils.openURI
import moe.styx.styx_common_compose.generated.resources.Res
import moe.styx.styx_common_compose.generated.resources.al
import moe.styx.styx_common_compose.generated.resources.mal
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun DesktopPlayerSection() {
    Column(Modifier.padding(4.dp)) {
        Text("The player is mpv with a modified uosc UI and custom configs provided by the package this app downloads.")
        Text("Controlled via a custom tool written in go on Windows and via junixsocket on Linux.")
        FlowRow(itemVerticalAlignment = Alignment.CenterVertically) {
            BasicChip("mpv.io", Icons.Default.PlayCircle, "Player") { openURI("https://mpv.io/") }
            BasicChip("uosc", Icons.Default.FormatPaint, "Player UI") { openURI("https://github.com/tomasklaen/uosc") }
            BasicChip(
                "mpv-ipc-bridge",
                Icons.Default.Memory,
                "Control tool"
            ) { openURI("https://github.com/Vodes/mpv-ipc-bridge") }
        }
    }
}

@Composable
internal fun AndroidPlayerSection() {
    Column(Modifier.padding(4.dp)) {
        Text("The player uses the libmpv-android library by the Findroid author with a custom Styx UI on top.")
        Text("This just embeds the mpv player into an android view.")
        FlowRow(itemVerticalAlignment = Alignment.CenterVertically) {
            BasicChip(
                "libmpv-android",
                Icons.Default.DeveloperBoard,
                "libmpv android library"
            ) { openURI("https://github.com/jarnedemeulemeester/libmpv-android") }
            BasicChip("mpv.io", Icons.Default.PlayCircle, "Player") { openURI("https://mpv.io/") }
        }
    }
}

@Composable
internal fun TrackingPlayerSection() {
    Column(Modifier.padding(4.dp)) {
        Text("Supported tracking sites, be it automatically, if enabled, or manually through their respective icons are AniList and MyAnimeList.")
        Text("Both use a custom API wrapper implementation specifically for Styx.")
        FlowRow(itemVerticalAlignment = Alignment.CenterVertically) {
            val alPainter = painterResource(Res.drawable.al)
            BasicChipPainter("AniList", alPainter, "Open AniList", enforceSize = 25.dp) { openURI("https://anilist.co") }
            val malPainter = painterResource(Res.drawable.mal)
            BasicChipPainter(
                "MyAnimeList",
                malPainter,
                "Open MyAnimeList",
                enforceSize = 25.dp
            ) { openURI("https://myanimelist.net") }
            BasicChip(
                "anilist-kmp",
                Icons.Default.IntegrationInstructions,
                "AniList API Wrapper"
            ) { openURI("https://github.com/Vodes/anilist-kmp") }
            BasicChip(
                "styx-libs-mal",
                Icons.Default.IntegrationInstructions,
                "MyAnimeList API Wrapper"
            ) { openURI("https://github.com/Vodes/Styx-Lib-MAL") }
        }
    }
}