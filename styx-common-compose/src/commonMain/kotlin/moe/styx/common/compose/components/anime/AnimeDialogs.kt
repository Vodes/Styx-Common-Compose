package moe.styx.common.compose.components.anime

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import moe.styx.common.compose.components.AppShapes
import moe.styx.common.compose.components.misc.OutlinedText
import moe.styx.common.compose.components.misc.TextWithCheckBox
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.files.collectWithEmptyInitial
import moe.styx.common.data.MediaEntry
import moe.styx.common.extension.eqI
import moe.styx.common.extension.toBoolean

@Composable
fun FailedDialog(message: String, modifier: Modifier = Modifier, buttonModifier: Modifier = Modifier, onDismiss: (Boolean) -> Unit = {}) {
    AlertDialog(
        { onDismiss(false) },
        modifier = modifier,
        title = { Text("Failed to start player") },
        text = { Text(message) },
        dismissButton = {
            Button({ onDismiss(false) }, modifier = buttonModifier) { Text("OK") }
        },
        confirmButton = {
            Button({ onDismiss(true) }, modifier = buttonModifier) { Text("Open Settings") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MediaInfoDialog(mediaEntry: MediaEntry, onDismiss: () -> Unit) {
    val mediaInfos by Storage.stores.mediainfoStore.collectWithEmptyInitial()
    val mediaInfo = mediaInfos.find { it.entryID eqI mediaEntry.GUID }
    BasicAlertDialog(onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(color = MaterialTheme.colorScheme.surface, shape = AppShapes.medium) {
            AnimatedVisibility(mediaInfo == null) {
                Column(Modifier.padding(10.dp)) {
                    Text("Could not find details on this file.")
                }
            }
            AnimatedVisibility(mediaInfo != null) {
                Column(Modifier.padding(10.dp)) {
                    if (mediaInfo != null) {
                        Text("Video Information", Modifier.padding(3.dp, 10.dp), style = MaterialTheme.typography.titleLarge)
                        Row(Modifier.padding(6.dp, 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedText("${mediaInfo.videoCodec} ${mediaInfo.videoBitdepth}-bit")
                            OutlinedText(mediaInfo.videoRes.split("x").getOrNull(1)?.let { "${it}p" } ?: mediaInfo.videoRes)
                        }
                        Text("Other Tracks", Modifier.padding(3.dp, 10.dp, 0.dp, 5.dp), style = MaterialTheme.typography.titleLarge)
                        FlowRow(Modifier.padding(6.dp, 2.dp), verticalArrangement = Arrangement.Center, horizontalArrangement = Arrangement.Start) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextWithCheckBox(
                                    "Has english dub",
                                    mediaInfo.hasEnglishDub.toBoolean(),
                                    enabled = false
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextWithCheckBox(
                                    "Has german dub",
                                    mediaInfo.hasGermanDub.toBoolean(),
                                    enabled = false
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextWithCheckBox(
                                    "Has german sub",
                                    mediaInfo.hasGermanSub.toBoolean(),
                                    enabled = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}