package moe.styx.common.compose.components.anime

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import moe.styx.common.compose.components.buttons.ExpandIconButton
import moe.styx.common.compose.components.misc.OutlinedText
import moe.styx.common.compose.viewmodels.MediaStorage
import moe.styx.common.data.Media

@Composable
fun MediaNameListing(media: Media, modifier: Modifier = Modifier) {
    SelectionContainer {
        Column(modifier.padding(10.dp, 20.dp)) {
            if (media.nameEN != null) {
                Text(
                    media.nameEN!!,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 3
                )
            }
            if (media.nameJP != null && (media.nameEN == null || !media.nameEN.equals(media.nameJP, true))) {
                Text(
                    media.nameJP!!,
                    Modifier.padding(2.dp, 10.dp),
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MediaGenreListing(media: Media) {
    var isExpanded by remember { mutableStateOf(false) }
    if (!media.genres.isNullOrBlank()) {
        FlowRow(Modifier.padding(5.dp), horizontalArrangement = Arrangement.Start, verticalArrangement = Arrangement.Center) {
            for (genre in media.genres!!.split(",")) {
                OutlinedText(genre.trim(), MaterialTheme.colorScheme.primary)
            }
            if (!media.tags.isNullOrBlank()) {
                ExpandIconButton(tooltip = "Show tags", tooltipExpanded = "Hide tags", isExpanded = isExpanded) { isExpanded = !isExpanded }
            }
        }
        if (!media.tags.isNullOrBlank()) {
            AnimatedVisibility(isExpanded) {
                FlowRow(Modifier.padding(5.dp, 1.dp)) {
                    media.tags!!.split(",").forEach { tag ->
                        OutlinedText(tag.trim(), MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

@Composable
fun MediaRelations(mediaStorage: MediaStorage, onClick: (Media) -> Unit) {
    Text("Relations", Modifier.padding(6.dp, 4.dp), style = MaterialTheme.typography.titleLarge)
    Column(Modifier.padding(5.dp, 2.dp)) {
        if (mediaStorage.hasPrequel()) {
            Column(Modifier.align(Alignment.Start)) {
                Text("Prequel", Modifier.padding(4.dp, 5.dp, 4.dp, 6.dp), style = MaterialTheme.typography.bodyMedium)
                AnimeListItem(mediaStorage.prequel!!, mediaStorage.prequelImage) { onClick(mediaStorage.prequel) }
            }
        }
        if (mediaStorage.hasSequel()) {
            Column(Modifier.align(Alignment.Start)) {
                Text("Sequel", Modifier.padding(4.dp, 5.dp, 4.dp, 6.dp), style = MaterialTheme.typography.bodyMedium)
                AnimeListItem(mediaStorage.sequel!!, mediaStorage.sequelImage) { onClick(mediaStorage.sequel) }
            }
        }
    }
}