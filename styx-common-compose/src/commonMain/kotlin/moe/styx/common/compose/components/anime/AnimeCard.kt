package moe.styx.common.compose.components.anime

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.navigator.Navigator
import com.russhwolf.settings.get
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import io.kamel.image.config.LocalKamelConfig
import io.ktor.http.*
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
    var showName by remember { mutableStateOf(showNamesAllTheTime) }
    val entries = if (showUnseenBadge) {
        val watched by Storage.stores.watchedStore.getCurrentAndCollectFlow()
        Storage.entryList.filter { it.mediaID == media.GUID }
            .associateWith { m -> watched.find { it.entryID == m.GUID } }.filter { (it.value?.maxProgress ?: 0F) < 85F }
    } else emptyMap()
    val shadowAlpha: Float by animateFloatAsState(if (showName) 0.8f else 0f)
    val textAlpha: Float by animateFloatAsState(if (showName) 1.0f else 0f)
    Card(modifier = Modifier.padding(2.dp).aspectRatio(0.71F), onClick = {
//        val view = if (media.isSeries.toBoolean()) AnimeDetailView(media.GUID) else MovieDetailView(media.GUID)
//        if (nav.lastItem is AnimeDetailView) {
//            nav.replace(view)
//        } else {
//            nav.push(view)
//        }
    }) {
        Box(contentAlignment = Alignment.Center) {
            if (image != null) {
                CompositionLocalProvider(LocalKamelConfig provides kamelConfig) {
                    val painter = if (image.isCached()) {
                        asyncPainterResource(image.getPath(), filterQuality = FilterQuality.Low)
                    } else asyncPainterResource(Url(image.getURL()), filterQuality = FilterQuality.Low)
                    KamelImage(
                        painter,
                        contentDescription = media.name,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.padding(2.dp).align(Alignment.Center)
                            .onPointerEvent(PointerEventType.Enter) { showName = !showNamesAllTheTime }
                            .onPointerEvent(PointerEventType.Exit) { showName = showNamesAllTheTime }.clip(RoundedCornerShape(8.dp))
                    )
                }
            }
            if (showUnseenBadge) {
                if (entries.isNotEmpty()) {
                    AnimeCardBadge(entries.size, Modifier.align(Alignment.TopEnd).zIndex(3f))
                }
            }
            if (showName || textAlpha > 0) {
                AnimeCardName(Modifier.onPointerEvent(PointerEventType.Enter) { showName = !showNamesAllTheTime }
                    .onPointerEvent(PointerEventType.Exit) { showName = showNamesAllTheTime },
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