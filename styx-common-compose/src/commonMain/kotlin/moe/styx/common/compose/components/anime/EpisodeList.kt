package moe.styx.common.compose.components.anime

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import moe.styx.common.compose.components.buttons.IconButtonWithTooltip
import moe.styx.common.compose.extensions.dynamicClick
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.files.collectWithEmptyInitial
import moe.styx.common.compose.files.updateList
import moe.styx.common.compose.http.login
import moe.styx.common.compose.threads.DownloadQueue
import moe.styx.common.compose.threads.RequestQueue
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.compose.viewmodels.MainDataViewModelStorage
import moe.styx.common.compose.viewmodels.MediaStorage
import moe.styx.common.data.MediaEntry
import moe.styx.common.data.MediaWatched
import moe.styx.common.extension.currentUnixSeconds
import moe.styx.common.extension.eqI
import moe.styx.common.util.SYSTEMFILES
import moe.styx.common.util.launchThreaded

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EpisodeList(
    storage: MainDataViewModelStorage,
    mediaStorage: MediaStorage,
    showSelection: MutableState<Boolean>,
    settingsView: Screen?,
    listState: LazyListState? = null,
    onPlay: (MediaEntry) -> String,
    headerContent: (@Composable ColumnScope.() -> Unit)? = null
) {
    val nav = LocalGlobalNavigator.current
    val associatedEntries = remember(storage) {
        mediaStorage.entries.map { ent -> ent to storage.watchedList.find { it.entryID eqI ent.GUID } }
    }

    val downloaded by Storage.stores.downloadedStore.collectWithEmptyInitial()
    val downloadQueue by DownloadQueue.queuedEntries.collectAsState()
    val currentlyDownloading by DownloadQueue.currentDownload.collectAsState()
    Column(Modifier.fillMaxHeight().fillMaxWidth()) {
        val selected = remember { mutableStateMapOf<String, Boolean>() }
        var needsRepaint by remember { mutableStateOf(0) }
        if (!showSelection.value)
            selected.clear()
        AnimatedVisibility(showSelection.value) { SelectedCard(selected, mediaStorage.entries) { needsRepaint++ } }

        var failedToPlayMessage by remember { mutableStateOf("") }
        if (failedToPlayMessage.isNotBlank()) {
            FailedDialog(
                failedToPlayMessage,
                Modifier.fillMaxWidth(0.6F),
                Modifier.align(Alignment.CenterHorizontally)
            ) {
                failedToPlayMessage = ""
                if (it && settingsView != null) nav.push(settingsView)
            }
        }
        var selectedMedia by remember { mutableStateOf<MediaEntry?>(null) }
        var showMediaInfoDialog by remember { mutableStateOf(false) }

        if (showMediaInfoDialog && selectedMedia != null) {
            MediaInfoDialog(selectedMedia!!) { showMediaInfoDialog = false }
        }

        val lazyListState = listState ?: rememberLazyListState()

        LazyColumn(state = lazyListState) {
            if (headerContent != null) {
                item("header") {
                    Column(Modifier.fillMaxWidth()) { headerContent() }
                }
            }
            itemsIndexed(associatedEntries, key = { _, item -> item.first.GUID }) { idx, item ->
                Column(Modifier.fillMaxWidth()) {
                    EpisodeListItem(
                        item.first, item.second,
                        Modifier.defaultMinSize(0.dp, 75.dp)
                            .dynamicClick(regularClick = {
                                onPlay(item.first)
                            })
                    ) {
                        showMediaInfoDialog = true
                        selectedMedia = item.first
                    }
                    if (idx != associatedEntries.size - 1) {
                        HorizontalDivider(
                            Modifier.fillMaxWidth().padding(0.dp, 5.dp, 0.dp, 0.dp),
                            thickness = 2.dp,
                            color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SelectedCard(selected: SnapshotStateMap<String, Boolean>, entries: List<MediaEntry>, onUpdate: () -> Unit) {
    val downloaded by Storage.stores.downloadedStore.collectWithEmptyInitial()
    val queued by DownloadQueue.queuedEntries.collectAsState()
    ElevatedCard(Modifier.padding(4.dp).fillMaxWidth().height(30.dp)) {
        Row {
            Text(
                if (selected.containsValue(true)) "Selected: ${selected.filter { it.value }.size}" else "Selection",
                modifier = Modifier.padding(6.dp, 5.dp).weight(1f), style = MaterialTheme.typography.labelMedium
            )
            IconButtonWithTooltip(Icons.Default.Visibility, "Set Watched") {
                val current = selected.filter { it.value }
                if (current.isEmpty())
                    return@IconButtonWithTooltip
                if (current.size == 1) {
                    val entry = entries.find { selected.entries.first().key eqI it.GUID }
                    if (entry == null)
                        return@IconButtonWithTooltip
                    RequestQueue.updateWatched(
                        MediaWatched(entry.GUID, login?.userID ?: "", currentUnixSeconds(), 0, 0F, 100F)
                    )
                } else {
                    RequestQueue.addMultipleWatched(
                        current
                            .map { pair -> entries.find { pair.key eqI it.GUID } }
                            .filterNotNull())
                }
                onUpdate()
            }
            IconButtonWithTooltip(Icons.Default.VisibilityOff, "Set Unwatched") {
                val current = selected.filter { it.value }
                if (current.isEmpty())
                    return@IconButtonWithTooltip
                if (current.size == 1) {
                    val entry = entries.find { selected.entries.first().key eqI it.GUID }
                    if (entry == null)
                        return@IconButtonWithTooltip
                    RequestQueue.removeWatched(entry)
                } else {
                    RequestQueue.removeMultipleWatched(
                        current
                            .map { pair -> entries.find { pair.key eqI it.GUID } }
                            .filterNotNull())
                }
                onUpdate()
            }

            IconButtonWithTooltip(Icons.Default.DownloadForOffline, "Download") {
                val current = selected.filter { it.value }
                if (current.isEmpty())
                    return@IconButtonWithTooltip
                if (current.size == 1) {
                    val entry = entries.find { selected.entries.first().key eqI it.GUID }
                    if (entry == null)
                        return@IconButtonWithTooltip
                    DownloadQueue.addToQueue(entry)
                } else {
                    DownloadQueue.addToQueue(
                        current
                            .map { pair -> entries.find { pair.key eqI it.GUID } }
                            .filterNotNull())
                }
            }
            IconButtonWithTooltip(Icons.Default.Delete, "Delete Downloaded") {
                val current = selected.filter { it.value }
                if (current.isEmpty())
                    return@IconButtonWithTooltip

                val currentSelected = current.map { pair -> entries.find { pair.key eqI it.GUID } }.filterNotNull()
                currentSelected.forEach { entry ->
                    if (queued.contains(entry.GUID)) {
                        launchThreaded {
                            DownloadQueue.queuedEntries.emit(
                                queued.toMutableList().filterNot { it eqI entry.GUID }.toList()
                            )
                        }
                    }
                    val downloadedEntry = downloaded.find { it.entryID eqI entry.GUID }
                    if (downloadedEntry != null) {
                        SYSTEMFILES.delete(downloadedEntry.okioPath)
                    }
                    launchThreaded {
                        Storage.stores.downloadedStore.updateList { list ->
                            list.removeAll { it.entryID eqI entry.GUID }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WatchedIndicator(mediaWatched: MediaWatched, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        LinearProgressIndicator(
            progress = { mediaWatched.progressPercent / 100 },
            modifier = Modifier.fillMaxWidth().weight(1f).padding(7.dp, 2.dp),
            trackColor = MaterialTheme.colorScheme.surfaceColorAtElevation(12.dp)
        )
        if (mediaWatched.maxProgress > 85)
            Icon(
                Icons.Default.CheckCircle,
                "Has been watched",
                Modifier.padding(0.dp, 0.dp, 6.dp, 0.dp).size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
    }
}