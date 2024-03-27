package moe.styx.common.compose.components.anime

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.core.screen.Screen
import com.russhwolf.settings.get
import moe.styx.common.compose.components.buttons.IconButtonWithTooltip
import moe.styx.common.compose.components.misc.ExpandableText
import moe.styx.common.compose.extensions.dynamicClick
import moe.styx.common.compose.extensions.readableSize
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.files.collectWithEmptyInitial
import moe.styx.common.compose.http.login
import moe.styx.common.compose.settings
import moe.styx.common.compose.threads.DownloadProgress
import moe.styx.common.compose.threads.DownloadQueue
import moe.styx.common.compose.threads.DownloadedEntry
import moe.styx.common.compose.threads.RequestQueue
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.data.MediaEntry
import moe.styx.common.data.MediaWatched
import moe.styx.common.extension.currentUnixSeconds
import moe.styx.common.extension.eqI
import moe.styx.common.extension.toDateString

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EpisodeList(
    episodes: List<MediaEntry>,
    showSelection: MutableState<Boolean>,
    settingsView: Screen?,
    listState: LazyListState? = null,
    onPlay: (MediaEntry) -> String,
    headerContent: (@Composable ColumnScope.() -> Unit)? = null
) {
    val nav = LocalGlobalNavigator.current
    val watchedList by Storage.stores.watchedStore.collectWithEmptyInitial()
    val downloaded by Storage.stores.downloadedStore.collectWithEmptyInitial()
    val downloadQueue by DownloadQueue.queuedEntries.collectAsState()
    val currentlyDownloading by DownloadQueue.currentDownload.collectAsState()
    Column(Modifier.fillMaxHeight().fillMaxWidth()) {
        val selected = remember { mutableStateMapOf<String, Boolean>() }
        var needsRepaint by remember { mutableStateOf(0) }
        val preferGerman = settings["prefer-german-metadata", false]
        val showSummaries = settings["display-ep-synopsis", false]
        if (!showSelection.value)
            selected.clear()

        val watched = episodes.associateWith { ep -> watchedList.find { ep.GUID eqI it.entryID } }
        AnimatedVisibility(showSelection.value) { SelectedCard(selected, episodes) { needsRepaint++ } }

        var failedToPlayMessage by remember { mutableStateOf("") }
        if (failedToPlayMessage.isNotBlank()) {
            FailedDialog(failedToPlayMessage, Modifier.fillMaxWidth(0.6F), Modifier.align(Alignment.CenterHorizontally)) {
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
            items(episodes.size) { i ->
                val ep = episodes[i]
                Column(
                    Modifier.padding(10.dp, 5.dp).fillMaxWidth().defaultMinSize(0.dp, 75.dp)
                        .dynamicClick({
                            if (showSelection.value) {
                                selected[ep.GUID] = !(selected[ep.GUID] ?: false)
                                return@dynamicClick
                            }
                            onPlay(ep)
                        }) {
                            showSelection.value = !showSelection.value
                        }
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        val title = if (!ep.nameDE.isNullOrBlank() && preferGerman) ep.nameDE else ep.nameEN
                        val watchProgress = watched[ep]
                        SelectionCheckboxes(showSelection, selected, episodes, i)
                        Column(Modifier.fillMaxWidth()) {
                            EpisodeDetailComp(
                                ep,
                                title,
                                watchProgress,
                                showSummaries,
                                preferGerman,
                                downloadQueue.contains(ep.GUID),
                                downloaded.find { it.entryID eqI ep.GUID },
                                currentlyDownloading?.let { if (it.entryID eqI ep.GUID) it else null }
                            ) {
                                selectedMedia = ep
                                showMediaInfoDialog = true
                            }
                        }
                    }
                }
                if (i < (episodes.size - 1))
                    HorizontalDivider(Modifier.fillMaxWidth(), thickness = 1.dp)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EpisodeDetailComp(
    ep: MediaEntry,
    title: String?,
    watchProgress: MediaWatched?,
    showSummaries: Boolean,
    preferGerman: Boolean,
    isQueued: Boolean,
    downloaded: DownloadedEntry? = null,
    progress: DownloadProgress? = null,
    onMediaInfoClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHover by interactionSource.collectIsHoveredAsState()

    Row {
        Column(modifier = Modifier.hoverable(interactionSource = interactionSource).weight(1f)) {
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
                    ep.fileSize.readableSize(),
                    Modifier.padding(5.dp).clickable { onMediaInfoClick() },
                    style = MaterialTheme.typography.labelSmall
                )
            }
            val summary = if (!ep.synopsisDE.isNullOrBlank() && preferGerman) ep.synopsisDE else ep.synopsisEN
            if (!summary.isNullOrBlank() && showSummaries)
                ExpandableText(summary, Modifier.padding(8.dp, 2.dp, 5.dp, 2.dp))
        }
        if (isQueued || downloaded != null || progress != null) {
            Column {
                if (isQueued) {
                    Icon(Icons.Default.Downloading, "Queued", modifier = Modifier.size(20.dp))
                } else if (progress != null) {
                    Box {
                        Icon(Icons.Default.Download, "Downloading", modifier = Modifier.size(17.dp).zIndex(1F))
                        CircularProgressIndicator({ progress.progressPercent.toFloat() / 100 }, modifier = Modifier.size(20.dp).zIndex(2F))
                    }
                } else {
                    Icon(Icons.Default.DownloadForOffline, "Downloaded", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
    AnimatedVisibility(watchProgress != null, enter = fadeIn(), exit = fadeOut()) {
        if (watchProgress != null) {
            WatchedIndicator(watchProgress, Modifier.fillMaxWidth().padding(0.dp, 2.dp, 0.dp, 5.dp))
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
                    RequestQueue.addMultipleWatched(current
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
                    RequestQueue.removeMultipleWatched(current
                        .map { pair -> entries.find { pair.key eqI it.GUID } }
                        .filterNotNull())
                }
                onUpdate()
            }

            IconButtonWithTooltip(Icons.Default.DownloadForOffline, "Download") {}
            IconButtonWithTooltip(Icons.Default.Delete, "Delete Downloaded") {}
        }
    }
}

@Composable
fun WatchedIndicator(mediaWatched: MediaWatched, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        LinearProgressIndicator(
            progress = { mediaWatched.progressPercent / 100 },
            modifier = Modifier.fillMaxWidth().weight(1f).padding(7.dp, 2.dp),
        )
        if (mediaWatched.maxProgress > 85)
            Icon(
                Icons.Default.CheckCircle,
                "Has been watched",
                Modifier.size(20.dp).padding(0.dp, 0.dp, 6.dp, 0.dp),
                tint = MaterialTheme.colorScheme.primary
            )
    }
}