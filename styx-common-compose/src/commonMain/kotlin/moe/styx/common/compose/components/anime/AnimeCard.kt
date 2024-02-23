package moe.styx.common.compose.components.anime

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.navigator.Navigator
import com.russhwolf.settings.get
import com.seiko.imageloader.rememberImagePainter
import moe.styx.common.compose.extensions.*
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.files.getCurrentAndCollectFlow
import moe.styx.common.compose.settings
import moe.styx.common.data.Media

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AnimeCard(nav: Navigator, media: Media, showUnseenBadge: Boolean = false) {
    val image = media.getThumb()
    val showNamesAllTheTime by remember { mutableStateOf(settings["display-names", false]) }
    val entries = if (showUnseenBadge) {
        val watched by Storage.stores.watchedStore.getCurrentAndCollectFlow()
        Storage.entryList.filter { it.mediaID == media.GUID }
            .associateWith { m -> watched.find { it.entryID == m.GUID } }.filter { (it.value?.maxProgress ?: 0F) < 85F }
    } else emptyMap()
//    val painter = image?.let {
//        if (image.isCached()) {
//            asyncPainterResource("file:/${image.getPath()}", filterQuality = FilterQuality.Low)
//        } else asyncPainterResource(Url(image.getURL()), filterQuality = FilterQuality.Low)
//    }
    Card(modifier = Modifier.padding(2.dp).aspectRatio(0.71F), onClick = {
        //TODO: Meme
    }) {
        var showName by remember { mutableStateOf(showNamesAllTheTime) }
        val shadowAlpha: Float by animateFloatAsState(if (showName) 0.8f else 0f)
        val textAlpha: Float by animateFloatAsState(if (showName) 1.0f else 0f)
        Box(contentAlignment = Alignment.Center) {
            if (image != null /* && painter != null*/) {
//                KamelImage(
//                    painter,
//                    contentDescription = media.name,
//                    contentScale = ContentScale.FillBounds,
//                    modifier = Modifier.padding(2.dp).align(Alignment.Center)
//                        .desktopPointerEvent({ showName = !showNamesAllTheTime }, { showName = showNamesAllTheTime })
//                        .clip(RoundedCornerShape(8.dp)),
//                    animationSpec = tween(),
//                    onLoading = { CircularProgressIndicator(it) }
//                )
                val painter = rememberImagePainter(if (image.isCached()) "file:/${image.getPath()}" else image.getURL())
                Image(
                    painter, media.name, contentScale = ContentScale.FillBounds,
                    modifier = Modifier.padding(2.dp).align(Alignment.Center)
                        .desktopPointerEvent({ showName = !showNamesAllTheTime }, { showName = showNamesAllTheTime })
                        .clip(RoundedCornerShape(8.dp))
                )
            }
            if (showUnseenBadge) {
                if (entries.isNotEmpty()) {
                    AnimeCardBadge(entries.size, Modifier.align(Alignment.TopEnd).zIndex(3f))
                }
            }
            if (showName || textAlpha > 0) {
                AnimeCardName(
                    Modifier.desktopPointerEvent({ showName = !showNamesAllTheTime }, { showName = showNamesAllTheTime }),
                    shadowAlpha,
                    textAlpha,
                    media.name
                )
            }
        }
    }
}

@Composable
internal fun AnimeCardBadge(num: Int, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier.clip(RoundedCornerShape(40)).size(33.dp).padding(4.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondary)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                num.toString(), softWrap = false,
                overflow = TextOverflow.Ellipsis,
                style = if (num.toString().length > 2) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
internal fun BoxScope.AnimeCardName(modifier: Modifier = Modifier, shadowAlpha: Float, textAlpha: Float, mediaName: String) {
    Surface(
        modifier = modifier.zIndex(1f).align(Alignment.BottomCenter).padding(0.dp, 0.dp, 0.dp, 5.dp)
            .defaultMinSize(0.dp, 25.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp)),
        color = MaterialTheme.colorScheme.surface.copy(shadowAlpha * 0.85F)
    ) {
        Surface(
            modifier = Modifier.zIndex(1f).align(Alignment.Center).padding(1.dp)
                .defaultMinSize(0.dp, 25.dp)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface.copy(shadowAlpha)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(5.dp)) {
                Text(
                    mediaName, modifier = Modifier.zIndex(2f).align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurface.copy(textAlpha), softWrap = false,
                    overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}