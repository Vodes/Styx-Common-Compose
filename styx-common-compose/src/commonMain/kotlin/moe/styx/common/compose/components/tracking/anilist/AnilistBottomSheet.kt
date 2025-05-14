package moe.styx.common.compose.components.tracking.anilist

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dokar.sonner.ToastType
import moe.styx.common.compose.components.buttons.IconButtonWithTooltip
import moe.styx.common.compose.utils.LocalToaster
import moe.styx.common.compose.viewmodels.MainDataViewModel
import moe.styx.common.compose.viewmodels.MediaStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnilistButtomSheet(
    mediaStorage: MediaStorage,
    mainVm: MainDataViewModel,
    sheetModel: AnilistBottomSheetModel,
    onURIClick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val toaster = LocalToaster.current
    val sheetState = rememberModalBottomSheetState()
    val media = remember(mediaStorage) { mediaStorage.media }
    val storage by mainVm.storageFlow.collectAsState()

    LaunchedEffect(mainVm.anilistUser, media.GUID) {
        sheetModel.fetchMediaState(mainVm, media)
    }
    val anilistData by sheetModel.anilistData.collectAsState()
    if (!sheetModel.errorString.isNullOrBlank()) {
        toaster.show(sheetModel.errorString!!, type = ToastType.Error)
        sheetModel.errorString = null
    }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (anilistData == null) {
                Text("Loading anilist data...", style = MaterialTheme.typography.headlineMedium)
            } else if (anilistData?.errored == true) {
                Text("Failed to load anilist data! Please send the logs to the admin.", style = MaterialTheme.typography.headlineMedium)
            } else {
                val mapped =
                    anilistData?.alMedia?.map { media -> media to anilistData?.userMedia?.find { it.media.id == media.id } }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("AniList", Modifier.padding(10.dp, 3.dp).weight(1f), style = MaterialTheme.typography.headlineMedium)
                    if (mapped != null && mainVm.anilistUser != null) {
                        IconButtonWithTooltip(Icons.Default.Sync, "Sync progress", modifier = Modifier.padding(5.dp, 0.dp)) {
                            syncAnilistProgress(media, mapped, mediaStorage.entries, storage.watchedList) {
                                sheetModel.updateRemoteStatus(mainVm, it, media)
                            }
                        }
                    }
                }

                if (mapped != null) {
                    mapped.forEach { mappedMedia ->
                        AnilistMediaComponent(mainVm.anilistUser, mappedMedia.first, mappedMedia.second, onURIClick) {
                            sheetModel.updateRemoteStatus(mainVm, it, media)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}