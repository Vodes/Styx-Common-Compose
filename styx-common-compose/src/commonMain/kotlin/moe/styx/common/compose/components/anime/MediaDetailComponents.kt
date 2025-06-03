package moe.styx.common.compose.components.anime

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import io.kamel.core.Resource
import io.kamel.image.KamelImage
import moe.styx.common.compose.components.AppShapes
import moe.styx.common.compose.components.tracking.anilist.AnilistBottomSheet
import moe.styx.common.compose.components.tracking.anilist.AnilistBottomSheetModel
import moe.styx.common.compose.components.tracking.mal.MALBottomSheet
import moe.styx.common.compose.components.tracking.mal.MALBottomSheetModel
import moe.styx.common.compose.extensions.getPainter
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.compose.utils.openURI
import moe.styx.common.compose.viewmodels.MainDataViewModel
import moe.styx.common.compose.viewmodels.MediaStorage
import moe.styx.common.data.tmdb.decodeMapping
import moe.styx.common.extension.toBoolean
import moe.styx.styx_common_compose.generated.resources.Res
import moe.styx.styx_common_compose.generated.resources.al
import moe.styx.styx_common_compose.generated.resources.mal
import moe.styx.styx_common_compose.generated.resources.tmdb
import org.jetbrains.compose.resources.painterResource

@Composable
fun StupidImageNameArea(
    mediaStorage: MediaStorage,
    modifier: Modifier = Modifier,
    mappingIconModifier: Modifier = Modifier.padding(8.dp, 3.dp).size(27.dp),
    dynamicMaxWidth: Dp = 760.dp,
    requiredMinHeight: Dp = 150.dp,
    requiredMaxHeight: Dp = 500.dp,
    requiredMaxWidth: Dp = 385.dp,
    enforceConstraints: Boolean = false,
    otherContent: @Composable () -> Unit = {}
) {
    val (media, img) = mediaStorage.media to mediaStorage.image
    val painter = img?.getPainter()
    BoxWithConstraints(modifier) {
        val width = this.maxWidth
        Row(Modifier.align(Alignment.TopStart).height(IntrinsicSize.Max).fillMaxWidth()) {
            if (width <= dynamicMaxWidth)
                BigScalingCardImage(
                    painter,
                    Modifier.fillMaxWidth().weight(1f, false),
                    cardModifier = Modifier.requiredHeightIn(requiredMinHeight, requiredMaxHeight).aspectRatio(0.71F)
                )
            else {
                // Theoretical max size that should be reached at this window width
                // Just force to not have layout spacing issues lmao
                if (enforceConstraints) {
                    BigScalingCardImage(painter, Modifier.requiredSize(requiredMaxWidth, requiredMaxHeight))
                } else {
                    BigScalingCardImage(painter, Modifier.fillMaxHeight(), cardModifier = Modifier.aspectRatio(0.71F))
                }
            }
            Column(Modifier.fillMaxWidth().weight(1f, true)) {
                MediaNameListing(media, Modifier.align(Alignment.Start))
                otherContent()
                Spacer(Modifier.weight(1f, true))
                MappingIcons(mediaStorage, mappingIconModifier)
            }
        }
    }
}

@Composable
fun BigScalingCardImage(image: Resource<Painter>?, modifier: Modifier = Modifier, cardModifier: Modifier = Modifier) {
    Column(modifier) {
        ElevatedCard(
            cardModifier.align(Alignment.Start).padding(12.dp),
        ) {
            if (image != null)
                KamelImage(
                    { image },
                    contentDescription = "Thumbnail",
                    modifier = Modifier.padding(2.dp).clip(AppShapes.small),
                    contentScale = ContentScale.FillBounds
                )
        }
    }
}

@Composable
fun MappingIcons(mediaStorage: MediaStorage, modifier: Modifier) {
    val mappings = mediaStorage.media.decodeMapping() ?: return
    val tmdbURL =
        mappings.tmdbMappings.minByOrNull { it.remoteID }?.remoteID?.let { "https://themoviedb.org/${if (mediaStorage.media.isSeries.toBoolean()) "tv" else "movie"}/$it" }
    val nav = LocalGlobalNavigator.current
    var showAnilistSheet by remember { mutableStateOf(false) }
    var showMalSheet by remember { mutableStateOf(false) }
    Row(Modifier.padding(0.dp, 0.dp, 0.dp, 15.dp), verticalAlignment = Alignment.CenterVertically) {
        val filter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
        if (mappings.anilistMappings.isNotEmpty())
            Image(
                painterResource(Res.drawable.al),
                "AniList",
                modifier.clip(AppShapes.small).clickable {
                    showAnilistSheet = true
                },
                contentScale = ContentScale.FillWidth,
                colorFilter = filter
            )
        if (mappings.malMappings.isNotEmpty())
            Image(
                painterResource(Res.drawable.mal),
                "MyAnimeList",
                modifier.clip(AppShapes.small).clickable {
                    showMalSheet = !showMalSheet
                },
                contentScale = ContentScale.FillWidth,
                colorFilter = filter
            )
        if (!tmdbURL.isNullOrBlank())
            Image(
                painterResource(Res.drawable.tmdb),
                "TheMovieDB",
                modifier.clip(AppShapes.small).clickable {
                    openURI(tmdbURL)
                },
                contentScale = ContentScale.FillWidth,
                colorFilter = filter
            )
    }
    if (showAnilistSheet) {
        val state = nav.rememberNavigatorScreenModel("al-sheet-${mediaStorage.media.GUID}") { AnilistBottomSheetModel() }
        val sm = nav.rememberNavigatorScreenModel("main-vm") { MainDataViewModel() }
        AnilistBottomSheet(mediaStorage, sm, state) {
            showAnilistSheet = false
        }
    }
    if (showMalSheet) {
        val state = nav.rememberNavigatorScreenModel("mal-sheet-${mediaStorage.media.GUID}") { MALBottomSheetModel() }
        val sm = nav.rememberNavigatorScreenModel("main-vm") { MainDataViewModel() }
        MALBottomSheet(mediaStorage, sm, state) {
            showMalSheet = false
        }
    }
}