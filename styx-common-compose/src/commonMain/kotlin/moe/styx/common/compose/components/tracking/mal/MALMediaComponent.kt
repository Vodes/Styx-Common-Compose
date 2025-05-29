package moe.styx.common.compose.components.tracking.mal

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
import moe.styx.libs.mal.types.MALMedia
import moe.styx.libs.mal.types.MALUser

@Composable
fun MALMediaComponent(
    malUser: MALUser?,
    malMedia: MALMedia,
    isEnabled: Boolean,
    onStatusUpdate: (CommonMediaStatus) -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        var status by remember(malUser, malMedia.listStatus) {
            mutableStateOf(
                CommonMediaStatus(
                    -1,
                    malMedia.id,
                    malMedia.listStatus?.let { CommonMediaListStatus.fromMalStatus(it.status, it.isRewatching) } ?: CommonMediaListStatus.NONE,
                    malMedia.listStatus?.watchedEpisodes ?: -1,
                    malMedia.numEpisodes ?: Int.MAX_VALUE
                )
            )
        }

        RemoteMediaComponent(
            malMedia.title,
            malMedia.mainPicture.large ?: malMedia.mainPicture.medium,
            "https://myanimelist.net/anime/${malMedia.id}",
            malUser != null,
            isEnabled,
            status
        ) {
            onStatusUpdate(it)
        }
    }
}