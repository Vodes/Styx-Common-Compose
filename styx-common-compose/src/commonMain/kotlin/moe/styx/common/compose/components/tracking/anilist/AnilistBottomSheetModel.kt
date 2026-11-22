package moe.styx.common.compose.components.tracking.anilist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import moe.styx.common.compose.components.tracking.common.CommonMediaStatus
import moe.styx.common.compose.components.tracking.common.TrackingSheetRequests
import moe.styx.common.compose.components.tracking.common.trackingErrorMessage
import moe.styx.common.compose.navigation.ScreenModel
import moe.styx.common.compose.navigation.screenModelScope
import moe.styx.common.compose.viewmodels.MainDataViewModel
import moe.styx.common.data.Media


class AnilistBottomSheetModel : ScreenModel {
    var errorString by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(true)

    private val requests = TrackingSheetRequests({ isLoading = it }, { errorString = it })

    private val _anilistData = MutableStateFlow<AlTrackingData?>(null)
    val anilistData = _anilistData.stateIn(screenModelScope, SharingStarted.WhileSubscribed(2000), null)


    fun fetchMediaState(mainVm: MainDataViewModel, media: Media) = screenModelScope.launch {
        runTrackingRequest {
            val result = AnilistTracking.fetchAnilistTrackingData(media, mainVm.anilistApiClient, mainVm.anilistUser)
            _anilistData.emit(result.getOrThrow())
        }
    }

    fun updateRemoteStatus(mainVm: MainDataViewModel, status: CommonMediaStatus, media: Media) = screenModelScope.launch {
        runTrackingRequest {
            val result = AnilistTracking.updateRemoteStatus(status, mainVm.anilistApiClient, mainVm.anilistUser)
            if (result.success)
                fetchMediaState(mainVm, media).join()
            else
                errorString = trackingErrorMessage(result.message)
        }
    }

    internal suspend fun runTrackingRequest(action: suspend () -> Unit) =
        requests.run(action)
}
