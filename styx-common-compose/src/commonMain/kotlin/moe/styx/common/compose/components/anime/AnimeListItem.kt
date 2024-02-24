package moe.styx.common.compose.components.anime

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import io.ktor.http.*
import moe.styx.common.compose.components.AppShapes
import moe.styx.common.compose.extensions.getPath
import moe.styx.common.compose.extensions.getThumb
import moe.styx.common.compose.extensions.getURL
import moe.styx.common.compose.extensions.isCached
import moe.styx.common.data.Media

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeListItem(media: Media, onClick: () -> Unit) {
    val image = media.getThumb()
    val painter = image?.let {
        if (image.isCached()) {
            asyncPainterResource("file:/${image.getPath()}", key = image.GUID, filterQuality = FilterQuality.Low)
        } else asyncPainterResource(Url(image.getURL()), key = image.GUID, filterQuality = FilterQuality.Low)
    }
    ElevatedCard(
        modifier = Modifier.padding(5.dp, 2.dp).fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(3.dp),
        onClick = onClick
    ) {
        Row(Modifier.height(80.dp)) {
            ElevatedCard(
                Modifier.clip(AppShapes.large).width(67.dp).padding(3.dp),
            ) {
                if (image != null && painter != null) {
                    KamelImage(
                        painter,
                        contentDescription = media.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.padding(1.dp),
                        animationSpec = tween(),
                        onLoading = { CircularProgressIndicator(it) }
                    )
                }
            }
            Column {
                Text(
                    media.name,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    modifier = Modifier.padding(10.dp, 3.dp, 0.dp, 0.dp)
                )
                if (media.nameEN != null && !media.name.equals(media.nameEN, true)) {
                    Text(
                        media.nameEN!!,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false,
                        modifier = Modifier.padding(10.dp, 3.dp, 0.dp, 0.dp).align(Alignment.Start),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                if (media.nameJP != null && !media.name.equals(media.nameJP, true) && !media.nameJP.equals(
                        media.nameEN,
                        true
                    )
                ) {
                    Text(
                        media.nameJP!!,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false,
                        modifier = Modifier.padding(10.dp, 3.dp, 0.dp, 0.dp).align(Alignment.Start),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}