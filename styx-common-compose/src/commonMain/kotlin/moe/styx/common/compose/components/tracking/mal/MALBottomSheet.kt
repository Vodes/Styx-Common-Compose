package moe.styx.common.compose.components.tracking.mal

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
fun MALBottomSheet(
    mediaStorage: MediaStorage,
    mainVm: MainDataViewModel,
    sheetModel: MALBottomSheetModel,
    onDismiss: () -> Unit
) {
    val toaster = LocalToaster.current
    val sheetState = rememberModalBottomSheetState()
    val media = remember(mediaStorage) { mediaStorage.media }
    val storage by mainVm.storageFlow.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(mainVm.malUser, media.GUID) {
        if (mainVm.malUser == null)
            sheetModel.fetchMediaStateNonAuth(media)
        else
            sheetModel.fetchMediaState(mainVm, media)
    }
    val malData by sheetModel.malData.collectAsState()
    if (!sheetModel.errorString.isNullOrBlank()) {
        toaster.show(sheetModel.errorString!!, type = ToastType.Error)
        sheetModel.errorString = null
    }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (malData == null) {
                Text("Loading MAL data...", style = MaterialTheme.typography.headlineMedium)
            } else if (malData == null && !sheetModel.errorString.isNullOrBlank()) {
                Text(sheetModel.errorString!!, style = MaterialTheme.typography.headlineMedium)
            } else {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("MyAnimeList", Modifier.padding(10.dp, 3.dp).weight(1f), style = MaterialTheme.typography.headlineMedium)
                    if (sheetModel.isLoading)
                        LinearProgressIndicator(
                            modifier = Modifier.padding(7.dp, 3.dp).width(25.dp),
                            trackColor = MaterialTheme.colorScheme.surfaceColorAtElevation(12.dp)
                        )
                    if (malData != null && mainVm.malUser != null) {
                        IconButtonWithTooltip(Icons.Default.Sync, "Sync progress", modifier = Modifier.padding(5.dp, 0.dp)) {
                            scope.launch {
                                sheetModel.isLoading = true
                                val result = MALTracking.syncMALProgress(
                                    mediaStorage,
                                    storage.watchedList,
                                    malData,
                                    mainVm.malApiClient,
                                    mainVm.malUser
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

                if (!malData.isNullOrEmpty()) {
                    malData!!.sortedBy { it.id }.forEach { malMedia ->
                        MALMediaComponent(mainVm.malUser, malMedia, !sheetModel.isLoading) {
                            sheetModel.updateRemoteStatus(mainVm, it, media)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}