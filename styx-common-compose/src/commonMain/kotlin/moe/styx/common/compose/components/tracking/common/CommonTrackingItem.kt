package moe.styx.common.compose.components.tracking.common

import androidx.compose.animation.core.tween
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNewOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.kamel.image.KamelImage
import moe.styx.common.compose.components.AppShapes
import moe.styx.common.compose.components.buttons.IconButtonWithTooltip
import moe.styx.common.compose.components.tracking.common.rating.RatingComponent
import moe.styx.common.compose.extensions.clickableNoIndicator
import moe.styx.common.compose.extensions.getPainter
import moe.styx.common.compose.utils.openURI
import moe.styx.common.data.Image
import moe.styx.common.extension.capitalize
import moe.styx.common.extension.eqI
import moe.styx.common.extension.toInt
import pw.vodes.anilistkmp.graphql.type.ScoreFormat
import kotlin.math.max

@Composable
fun RemoteMediaComponent(
    title: String,
    imageURL: String,
    remoteURL: String,
    isLoggedIn: Boolean,
    isEnabled: Boolean,
    scoreFormat: ScoreFormat,
    status: CommonMediaStatus,
    onStatusUpdate: (CommonMediaStatus) -> Unit = {}
) {
    var showStatusDialog by remember { mutableStateOf(false) }
    var showEpisodeDialog by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    ElevatedCard(elevation = CardDefaults.elevatedCardElevation(4.dp)) {
        val isHover by interactionSource.collectIsHoveredAsState()
        Row(Modifier.hoverable(interactionSource)) {
            val image = Image(
                imageURL.substringAfterLast("/"),
                hasJPG = false.toInt(),
                hasWEBP = false.toInt(),
                hasPNG = false.toInt(),
                externalURL = imageURL
            )
            val painter = image.getPainter()
            Box(
                Modifier.padding(10.dp).clip(AppShapes.large).width(96.dp).heightIn(0.dp, 145.dp).fillMaxHeight().clickableNoIndicator {
                    openURI(imageURL)
                }
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
                    Text(
                        title,
                        modifier = Modifier.weight(1f).let {
                            if (isHover)
                                it.basicMarquee(repeatDelayMillis = 300)
                            else
                                it
                        },
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButtonWithTooltip(Icons.Filled.OpenInNewOff, "Open website") {
                        openURI(remoteURL)
                    }
                }

                Row(Modifier.fillMaxWidth()) {
                    ElevatedSurface(
                        modifier = Modifier.padding(3.dp).heightIn(36.dp, Dp.Unspecified),
                        onClick = { showStatusDialog = true },
                        enabled = isLoggedIn && isEnabled,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
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

                    ElevatedSurface(
                        modifier = Modifier.padding(3.dp).heightIn(36.dp, Dp.Unspecified),
                        onClick = { showEpisodeDialog = true },
                        enabled = isLoggedIn && isEnabled,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            (if (status.hasProgress) status.progress.toString() else "0") + " / " + (if (status.hasKnownMax) status.knownMax.toString() else "?"),
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.defaultMinSize(100.dp).padding(5.dp)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    ElevatedSurface(
                        modifier = Modifier.padding(3.dp, 3.dp, 11.dp, 3.dp).heightIn(36.dp, Dp.Unspecified),
                        enabled = if (status.hasProgress && status.hasKnownMax && isLoggedIn)
                            status.progress < status.knownMax
                        else
                            isLoggedIn && isEnabled,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = {
                            onStatusUpdate(
                                status.copy(
                                    status = if (status.status != CommonMediaListStatus.COMPLETED) CommonMediaListStatus.WATCHING else status.status,
                                    progress = max(status.progress, 0) + 1
                                )
                            )
                        }
                    ) {
                        Text(
                            "+1",
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.defaultMinSize(10.dp).padding(5.dp)
                        )
                    }
                }
                Row(Modifier.fillMaxWidth()) {
                    RatingComponent(status.score ?: 0.0F, scoreFormat, isLoggedIn && isEnabled && status.progress > 0) {
                        onStatusUpdate(status.copy(score = it))
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

@Composable
fun ElevatedSurface(
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.elevatedShape,
    enabled: Boolean = true,
    elevation: Dp = 0.dp,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    disabledContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    val color = if (enabled) containerColor else disabledContainerColor
    val contentColor = if (enabled)
        contentColorFor(containerColor)
    else
        contentColorFor(containerColor).copy(0.38f)

    val surfaceModifier = modifier.clip(shape).let {
        if (onClick != null)
            it.clickable(enabled) { onClick() }
        else it
    }
    Surface(
        surfaceModifier,
        color = color,
        contentColor = contentColor,
        tonalElevation = elevation,
        shadowElevation = elevation
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            content()
        }
    }
}