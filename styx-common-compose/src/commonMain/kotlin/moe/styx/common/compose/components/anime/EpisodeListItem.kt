package moe.styx.common.compose.components.anime

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.russhwolf.settings.get
import moe.styx.common.compose.components.misc.ExpandableText
import moe.styx.common.compose.extensions.readableSize
import moe.styx.common.compose.settings
import moe.styx.common.data.MediaEntry
import moe.styx.common.data.MediaWatched
import moe.styx.common.extension.toDateString

@Composable
fun LazyItemScope.EpisodeListItem(
    item: MediaEntry,
    watched: MediaWatched?,
    modifier: Modifier = Modifier,
    showDivider: Boolean = false,
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
                textModifier,
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
                if (showDivider)
                    HorizontalDivider(
                        Modifier.fillMaxWidth().padding(0.dp, 9.dp, 0.dp, 0.dp),
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp)
                    )
            }
        },
        overlineContent = { EpisodeListItemOverline(item, onMediaInfoClick = onMediaInfoClick) },
        modifier = modifier.hoverable(interactionSource),
    )
}

@Composable
fun EpisodeListItemOverline(item: MediaEntry, modifier: Modifier = Modifier, onMediaInfoClick: () -> Unit) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            item.timestamp.toDateString(),
            Modifier.padding(5.dp, 0.dp, 0.dp, 4.dp).weight(1f),
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            item.fileSize.readableSize(),
            Modifier.padding(5.dp).clickable { onMediaInfoClick() },
            style = MaterialTheme.typography.labelSmall
        )
    }
}