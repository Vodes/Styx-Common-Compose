package moe.styx.common.compose.components.anime

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.russhwolf.settings.get
import moe.styx.common.compose.components.misc.ExpandableText
import moe.styx.common.compose.extensions.onRightClick
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.files.getCurrentAndCollectFlow
import moe.styx.common.compose.http.login
import moe.styx.common.compose.settings
import moe.styx.common.compose.threads.RequestQueue
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.data.MediaEntry
import moe.styx.common.data.MediaWatched
import moe.styx.common.extension.currentUnixSeconds
import moe.styx.common.extension.eqI
import moe.styx.common.extension.toDateString

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EpisodeList(episodes: List<MediaEntry>, showSelection: MutableState<Boolean>, settingsView: Screen?, onPlay: (MediaEntry) -> Unit) {
    val nav = LocalGlobalNavigator.current
    val watchedList by Storage.stores.watchedStore.getCurrentAndCollectFlow()
    Column(Modifier.fillMaxHeight().fillMaxWidth()) {
        val selected = remember { mutableStateMapOf<String, Boolean>() }
        var needsRepaint by remember { mutableStateOf(0) }
        val preferGerman = settings["prefer-german-metadata", false]
        val showSummaries = settings["display-ep-synopsis", false]
        if (!showSelection.value)
            selected.clear()

        val watched = episodes.associateWith { ep -> watchedList.find { ep.GUID eqI it.entryID } }

        AnimatedVisibility(showSelection.value) { SelectedCard(selected, episodes) { needsRepaint++ } }

        var showFailedDialog by remember { mutableStateOf(false) }
        var failedToPlayMessage by remember { mutableStateOf("") }
        if (showFailedDialog) {
            FailedDialog(failedToPlayMessage, Modifier.fillMaxWidth(0.6F), Modifier.align(Alignment.CenterHorizontally)) {
                showFailedDialog = false
                if (it && settingsView != null) nav.push(settingsView)
            }
        }
        var selectedMedia by remember { mutableStateOf<MediaEntry?>(null) }
        var showMediaInfoDialog by remember { mutableStateOf(false) }

        if (showMediaInfoDialog && selectedMedia != null) {
            MediaInfoDialog(selectedMedia!!) { showMediaInfoDialog = false }
        }

        LazyColumn {
            items(episodes.size) { i ->
                val ep = episodes[i]
                Column(
                    Modifier.padding(10.dp, 5.dp).fillMaxWidth().defaultMinSize(0.dp, 75.dp)
                        .onRightClick {
                            showSelection.value = !showSelection.value
                            if (showSelection.value)
                                selected[ep.GUID] = !(selected[ep.GUID] ?: false)
                        }
                        .combinedClickable(onClick = {
                            if (showSelection.value) {
                                selected[ep.GUID] = !(selected[ep.GUID] ?: false)
                                return@combinedClickable
                            }
                            onPlay(ep)
                        }, onLongClick = {
                            showSelection.value = !showSelection.value
                        })
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        val title = if (!ep.nameDE.isNullOrBlank() && preferGerman) ep.nameDE else ep.nameEN
                        val watchProgress = watched[ep]
                        SelectionCheckboxes(showSelection, selected, episodes, i)

                        Column(Modifier.fillMaxWidth()) {
                            val interactionSource = remember { MutableInteractionSource() }
                            val isHover by interactionSource.collectIsHoveredAsState()

                            Column(modifier = Modifier.hoverable(interactionSource = interactionSource)) {
                                var modifier = Modifier.padding(5.dp)
                                if (isHover)
                                    modifier = modifier.basicMarquee(delayMillis = 300)
                                Text(
                                    "${ep.entryNumber}${if (!title.isNullOrBlank()) " - $title" else ""}",
                                    modifier,
                                    softWrap = false,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        ep.timestamp.toDateString(),
                                        Modifier.padding(5.dp, 0.dp, 0.dp, 4.dp).weight(1f),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        ep.fileSize.toString(),//.readableSize(),
                                        Modifier.padding(5.dp).clickable {
                                            selectedMedia = ep
                                            showMediaInfoDialog = true
                                        },
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                val summary = if (!ep.synopsisDE.isNullOrBlank() && preferGerman) ep.synopsisDE else ep.synopsisEN
                                if (!summary.isNullOrBlank() && showSummaries)
                                    ExpandableText(summary, Modifier.padding(8.dp, 2.dp, 5.dp, 2.dp))
                            }
                            if (watchProgress != null)
                                WatchedIndicator(watchProgress, Modifier.fillMaxWidth().padding(0.dp, 2.dp, 0.dp, 5.dp))
                        }
                    }
                }
                if (i < (episodes.size - 1))
                    Divider(Modifier.fillMaxWidth(), thickness = 1.dp)
            }
        }
    }
}

@Composable
fun SelectedCard(selected: SnapshotStateMap<String, Boolean>, entries: List<MediaEntry>, onUpdate: () -> Unit) {
    ElevatedCard(Modifier.padding(4.dp).fillMaxWidth().height(30.dp)) {
        Row {
            Text(
                if (selected.containsValue(true)) "Selected: ${selected.filter { it.value }.size}" else "Selection",
                modifier = Modifier.padding(6.dp, 5.dp).weight(1f), style = MaterialTheme.typography.labelMedium
            )
            IconButton(onClick = {
                val current = selected.filter { it.value }
                if (current.isEmpty())
                    return@IconButton
                if (current.size == 1) {
                    val entry = entries.find { selected.entries.first().key eqI it.GUID }
                    if (entry == null)
                        return@IconButton
                    RequestQueue.updateWatched(
                        MediaWatched(entry.GUID, login?.userID ?: "", currentUnixSeconds(), 0, 0F, 100F)
                    )
                } else {
                    RequestQueue.addMultipleWatched(current
                        .map { pair -> entries.find { pair.key eqI it.GUID } }
                        .filterNotNull())
                }
                onUpdate()
            }) { Icon(Icons.Default.Visibility, "Set Watched") }

            IconButton(onClick = {
                val current = selected.filter { it.value }
                if (current.isEmpty())
                    return@IconButton
                if (current.size == 1) {
                    val entry = entries.find { selected.entries.first().key eqI it.GUID }
                    if (entry == null)
                        return@IconButton
                    RequestQueue.removeWatched(entry)
                } else {
                    RequestQueue.removeMultipleWatched(current
                        .map { pair -> entries.find { pair.key eqI it.GUID } }
                        .filterNotNull())
                }
                onUpdate()
            }) { Icon(Icons.Default.VisibilityOff, "Set Unwatched") }

            IconButton(onClick = {
                println("Not implemented yet.")
            }) { Icon(Icons.Default.DownloadForOffline, "Download") }

            IconButton(onClick = {
                println("Not implemented yet.")
            }) { Icon(Icons.Default.Delete, "Delete downloaded") }
        }
    }
}

@Composable
fun WatchedIndicator(mediaWatched: MediaWatched, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        LinearProgressIndicator(mediaWatched.progressPercent / 100, Modifier.fillMaxWidth().weight(1f).padding(7.dp, 2.dp))
        if (mediaWatched.maxProgress > 85)
            Icon(
                Icons.Default.CheckCircle,
                "Has been watched",
                Modifier.size(20.dp).padding(0.dp, 0.dp, 6.dp, 0.dp),
                tint = MaterialTheme.colorScheme.primary
            )
    }
}