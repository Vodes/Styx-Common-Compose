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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.styx.common.compose.components.buttons.IconButtonWithTooltip
import moe.styx.common.compose.utils.LocalToaster
import moe.styx.common.compose.viewmodels.MainDataViewModel
import moe.styx.common.compose.viewmodels.MediaStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnilistBottomSheet(
    mediaStorage: MediaStorage,
    mainVm: MainDataViewModel,
    sheetModel: AnilistBottomSheetModel,
    onDismiss: () -> Unit
) {
    val toaster = LocalToaster.current
    val sheetState = rememberModalBottomSheetState()
    val media = remember(mediaStorage) { mediaStorage.media }
    val storage by mainVm.storageFlow.collectAsState()
    val scope = rememberCoroutineScope()

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
            } else if (anilistData == null && !sheetModel.errorString.isNullOrBlank()) {
                Text(sheetModel.errorString!!, style = MaterialTheme.typography.headlineMedium)
            } else {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("AniList", Modifier.padding(10.dp, 3.dp).weight(1f), style = MaterialTheme.typography.headlineMedium)
                    if (sheetModel.isLoading)
                        LinearProgressIndicator(
                            modifier = Modifier.padding(7.dp, 3.dp).width(25.dp),
                            trackColor = MaterialTheme.colorScheme.surfaceColorAtElevation(12.dp)
                        )
                    if (anilistData != null && mainVm.anilistUser != null) {
                        IconButtonWithTooltip(Icons.Default.Sync, "Sync progress", modifier = Modifier.padding(5.dp, 0.dp)) {
                            scope.launch {
                                sheetModel.isLoading = true
                                val result = AnilistTracking.syncAnilistProgress(
                                    mediaStorage,
                                    storage.watchedList,
                                    anilistData,
                                    mainVm.anilistApiClient,
                                    mainVm.anilistUser
                                )
                                if (!result.success)
                                    sheetModel.errorString = result.message!!.also { sheetModel.isLoading = false }
                                else {
                                    delay(300)
                                    sheetModel.fetchMediaState(mainVm, media).join()
                                }
                            }
                        }
                    }
                }

                if (anilistData != null) {
                    anilistData!!.entries.forEach { mappedMedia ->
                        AnilistMediaComponent(mainVm.anilistUser, mappedMedia.key, mappedMedia.value, !sheetModel.isLoading) {
                            sheetModel.updateRemoteStatus(mainVm, it, media)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}