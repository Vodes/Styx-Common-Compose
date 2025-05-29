package moe.styx.common.compose.components.tracking.common

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNewOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.kamel.image.KamelImage
import moe.styx.common.compose.components.AppShapes
import moe.styx.common.compose.components.buttons.IconButtonWithTooltip
import moe.styx.common.compose.extensions.getPainter
import moe.styx.common.data.Image
import moe.styx.common.extension.capitalize
import moe.styx.common.extension.eqI
import moe.styx.common.extension.toInt
import kotlin.math.max

@Composable
fun RemoteMediaComponent(
    title: String,
    imageURL: String,
    remoteURL: String,
    isLoggedIn: Boolean,
    isEnabled: Boolean,
    onUriClick: (String) -> Unit = {},
    status: CommonMediaStatus,
    onStatusUpdate: (CommonMediaStatus) -> Unit = {}
) {
    var showStatusDialog by remember { mutableStateOf(false) }
    var showEpisodeDialog by remember { mutableStateOf(false) }
    ElevatedCard(elevation = CardDefaults.elevatedCardElevation(4.dp)) {
        Row {
            val image = Image(
                imageURL.substringAfterLast("/"),
                hasJPG = false.toInt(),
                hasWEBP = false.toInt(),
                hasPNG = false.toInt(),
                externalURL = imageURL
            )
            val painter = image.getPainter()
            Box(
                Modifier.padding(10.dp).clip(AppShapes.large).width(96.dp).heightIn(0.dp, 145.dp).fillMaxHeight()
            ) {
                KamelImage(
                    { painter },
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    animationSpec = tween(),
                    onLoading = { CircularProgressIndicator(progress = { it }) }
                )
            }

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                    IconButtonWithTooltip(Icons.Filled.OpenInNewOff, "Open website") {
                        onUriClick(remoteURL)
                    }
                }

                Row(Modifier.fillMaxWidth()) {
                    ElevatedCard(
                        {
                            showStatusDialog = true
                        },
                        enabled = isLoggedIn && isEnabled,
                        modifier = Modifier.padding(3.dp),
                        colors = CardDefaults.elevatedCardColors(MaterialTheme.colorScheme.tertiaryContainer),
                        elevation = CardDefaults.elevatedCardElevation(1.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val statusName = status.status.name.capitalize()
                            Text(
                                if (status.status != CommonMediaListStatus.NONE) statusName else "/",
                                style = MaterialTheme.typography.labelLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.defaultMinSize(100.dp).padding(5.dp)
                            )
                        }
                    }

                    ElevatedCard(
                        {
                            showEpisodeDialog = true
                        },
                        enabled = isLoggedIn && isEnabled,
                        modifier = Modifier.padding(3.dp),
                        colors = CardDefaults.elevatedCardColors(MaterialTheme.colorScheme.tertiaryContainer),
                        elevation = CardDefaults.elevatedCardElevation(1.dp)
                    ) {
                        Text(
                            (if (status.hasProgress) status.progress.toString() else "0") + " / " + (if (status.hasKnownMax) status.knownMax.toString() else "?"),
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.defaultMinSize(100.dp).padding(5.dp)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    ElevatedCard(
                        {
                            onStatusUpdate(
                                status.copy(
                                    status = if (status.status != CommonMediaListStatus.COMPLETED) CommonMediaListStatus.WATCHING else status.status,
                                    progress = max(status.progress, 0) + 1
                                )
                            )
                        },
                        enabled = if (status.hasProgress && status.hasKnownMax && isLoggedIn)
                            status.progress < status.knownMax
                        else isLoggedIn && isEnabled,
                        modifier = Modifier.padding(3.dp),
                        colors = CardDefaults.elevatedCardColors(MaterialTheme.colorScheme.secondaryContainer),
                        elevation = CardDefaults.elevatedCardElevation(1.dp)
                    ) {
                        Text(
                            "+1",
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.defaultMinSize(10.dp).padding(5.dp)
                        )
                    }
                }
            }
        }
    }
    if (showStatusDialog) {
        StatusDialog(
            status.status.name.capitalize(),
            CommonMediaListStatus.entries.map { it.name },
            { showStatusDialog = false }) {
            onStatusUpdate(status.copy(status = CommonMediaListStatus.entries.find { s -> s.name eqI it }!!))
        }
    }

    if (showEpisodeDialog) {
        EpisodeDialog(status.progress, status.knownMax, { showEpisodeDialog = false }) {
            onStatusUpdate(
                status.copy(
                    status = if (status.status != CommonMediaListStatus.COMPLETED) CommonMediaListStatus.WATCHING else status.status,
                    progress = it
                )
            )
        }
    }
}