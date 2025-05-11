package moe.styx.common.compose.components.tracking.anilist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dokar.sonner.ToastType
import moe.styx.common.compose.utils.LocalToaster
import moe.styx.common.compose.viewmodels.MainDataViewModel
import moe.styx.common.data.Media

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnilistButtomSheet(media: Media, mainVm: MainDataViewModel, sheetModel: AnilistBottomSheetModel, onDismiss: () -> Unit) {
    val toaster = LocalToaster.current
    val sheetState = rememberModalBottomSheetState()
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
                if (mapped != null) {
                    mapped.forEach { mappedMedia ->
                        AnilistMediaComponent(mainVm.anilistUser, mappedMedia.first, mappedMedia.second) {
                            sheetModel.updateRemoteStatus(mainVm, it, media)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}