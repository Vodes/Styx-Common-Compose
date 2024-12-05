package moe.styx.common.compose.components.anime

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.russhwolf.settings.get
import moe.styx.common.compose.components.misc.ExpandableText
import moe.styx.common.compose.extensions.readableSize
import moe.styx.common.compose.settings
import moe.styx.common.compose.threads.DownloadProgress
import moe.styx.common.data.MediaEntry
import moe.styx.common.data.MediaWatched
import moe.styx.common.extension.toDateString

@Composable
fun LazyItemScope.EpisodeListItem(
    item: MediaEntry,
    watched: MediaWatched?,
    showCheckboxes: Boolean,
    selectedIDs: SnapshotStateList<String>,
    isDownloaded: Boolean = false,
    isInQueue: Boolean = false,
    downloadProgress: DownloadProgress? = null,
    modifier: Modifier = Modifier,
    onMediaInfoClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHover by interactionSource.collectIsHoveredAsState()
    val preferGerman = remember { settings["prefer-german-metadata", false] }
    val showSummaries = remember { settings["display-ep-synopsis", false] }
    ListItem(
        headlineContent = {
            val title = if (!item.nameDE.isNullOrBlank() && preferGerman) item.nameDE else item.nameEN
            var textModifier = Modifier.padding(3.dp, 1.dp)
            if (isHover)
                textModifier = textModifier.basicMarquee(repeatDelayMillis = 300)
            Text(
                item.entryNumber + if (title.isNullOrBlank()) "" else " - $title",
                textModifier.fillMaxWidth(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium
            )
        },
        supportingContent = {
            Column(Modifier.fillMaxWidth()) {
                val summary = if (preferGerman && !item.synopsisDE.isNullOrBlank()) item.synopsisDE else item.synopsisEN
                if (showSummaries && !summary.isNullOrBlank())
                    ExpandableText(summary)

                AnimatedVisibility(watched != null, enter = fadeIn(), exit = fadeOut()) {
                    if (watched != null) {
                        WatchedIndicator(watched, Modifier.fillMaxWidth().padding(2.dp, 5.dp))
                    }
                }
            }
        },
        leadingContent = {
            AnimatedVisibility(showCheckboxes) {
                Checkbox(selectedIDs.contains(item.GUID), {
                    if (selectedIDs.contains(item.GUID))
                        selectedIDs.remove(item.GUID)
                    else
                        selectedIDs.add(item.GUID)
                }, modifier = Modifier.padding(0.dp, 6.dp, 0.dp, 0.dp))
            }
        },
        overlineContent = {
            EpisodeListItemOverline(
                item,
                isDownloaded = isDownloaded,
                isInQueue = isInQueue,
                downloadProgress = downloadProgress,
                onMediaInfoClick = onMediaInfoClick
            )
        },
        modifier = modifier.hoverable(interactionSource),
        colors = ListItemDefaults.colors(MaterialTheme.colorScheme.surfaceContainerLow),
        tonalElevation = 0.dp
    )
}

@Composable
fun EpisodeListItemOverline(
    item: MediaEntry,
    modifier: Modifier = Modifier,
    isDownloaded: Boolean = false,
    isInQueue: Boolean = false,
    downloadProgress: DownloadProgress? = null,
    onMediaInfoClick: () -> Unit
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            item.timestamp.toDateString(),
            Modifier.padding(5.dp, 0.dp, 0.dp, 4.dp).weight(1f),
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            item.fileSize.readableSize(),
            Modifier.padding(5.dp).clickable { onMediaInfoClick() },
            style = MaterialTheme.typography.labelMedium
        )

        if (isDownloaded) {
            Icon(
                Icons.Filled.DownloadForOffline,
                "Is available offline",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(19.dp)
            )
        } else if (downloadProgress != null) {
            Box(Modifier.size(19.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    { downloadProgress.progressPercent / 100f },
                    modifier = Modifier.zIndex(5f).size(19.dp),
                    trackColor = MaterialTheme.colorScheme.onSurface,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    Icons.Filled.Download,
                    "Is downloading: ${downloadProgress.progressPercent}%",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(9.dp)
                )
            }
        } else if (isInQueue) {
            Icon(
                Icons.Filled.Downloading, "Is in downloader queue", tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}