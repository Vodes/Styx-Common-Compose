package moe.styx.common.compose.components.anime

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.kamel.image.KamelImage
import moe.styx.common.compose.components.AppShapes
import moe.styx.common.compose.extensions.getPainter
import moe.styx.common.data.Image
import moe.styx.common.data.Media

@Composable
fun AnimeListItem(media: Media, image: Image?, targetEpisodeNum: Int = 0, modifier: Modifier = Modifier.padding(5.dp, 2.dp), onClick: () -> Unit) {
    val painter = image?.getPainter()
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(Modifier.height(80.dp)) {
            ElevatedCard(
                Modifier.clip(AppShapes.large).width(67.dp).padding(3.dp),
            ) {
                if (image != null && painter != null) {
                    KamelImage(
                        { painter },
                        contentDescription = media.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.padding(1.dp),
                        animationSpec = tween(),
                        onLoading = { CircularProgressIndicator(progress = { it }) }
                    )
                }
            }
            Column(Modifier.weight(1f)) {
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
            if (targetEpisodeNum != 0) {
                SuggestionChip(onClick, {
                    Text("$targetEpisodeNum episodes")
                }, modifier = Modifier.padding(8.dp, 1.dp))
            }
        }
    }
}