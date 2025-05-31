package moe.styx.common.compose.components.tracking.anilist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.styx.common.compose.components.tracking.common.CommonMediaListStatus
import moe.styx.common.compose.components.tracking.common.CommonMediaStatus
import moe.styx.common.compose.components.tracking.common.RemoteMediaComponent
import moe.styx.common.compose.components.tracking.common.toCommon
import pw.vodes.anilistkmp.graphql.fragment.User
import pw.vodes.anilistkmp.graphql.type.ScoreFormat

@Composable
fun AnilistMediaComponent(
    viewer: User?,
    alMedia: AlMedia,
    entry: AlUserEntry? = null,
    isEnabled: Boolean = true,
    onStatusUpdate: (CommonMediaStatus) -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        var status by remember(viewer, entry?.listEntry) {
            mutableStateOf(
                CommonMediaStatus(
                    entry?.listEntry?.id ?: -1,
                    alMedia.id,
                    entry?.listEntry?.status?.toCommon() ?: CommonMediaListStatus.NONE,
                    entry?.listEntry?.progress ?: -1,
                    alMedia.episodes ?: Int.MAX_VALUE,
                    entry?.listEntry?.score?.toFloat()
                )
            )
        }

        RemoteMediaComponent(
            alMedia.title?.english ?: (alMedia.title?.romaji ?: ""),
            alMedia.coverImage?.large ?: "",
            "https://anilist.co/anime/${alMedia.id}",
            viewer != null,
            isEnabled,
            viewer?.mediaListOptions?.scoreFormat ?: ScoreFormat.POINT_10,
            status
        ) {
            onStatusUpdate(it)
        }
    }
}