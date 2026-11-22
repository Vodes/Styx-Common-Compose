package moe.styx.common.compose.components.tracking.mal

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
import moe.styx.common.compose.extensions.minimalSearchString
import moe.styx.common.compose.navigation.ScreenModel
import moe.styx.common.compose.navigation.screenModelScope
import moe.styx.common.compose.viewmodels.MainDataViewModel
import moe.styx.common.data.Media
import moe.styx.common.data.tmdb.decodeMapping
import moe.styx.libs.mal.JikanApiClient
import moe.styx.libs.mal.types.MALMedia

class MALBottomSheetModel : ScreenModel {
    var errorString by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(true)

    private val requests = TrackingSheetRequests({ isLoading = it }, { errorString = it })

    private val _malData = MutableStateFlow<List<MALMedia>?>(null)
    val malData = _malData.stateIn(screenModelScope, SharingStarted.WhileSubscribed(2000), null)

    fun fetchMediaStateNonAuth(media: Media) = screenModelScope.launch {
        runTrackingRequest {
            val mappings = media.decodeMapping()?.malMappings
            if (mappings.isNullOrEmpty()) {
                errorString = "No MyAnimeList ids found for: ${media.name}"
                return@runTrackingRequest
            }
            val result = JikanApiClient.searchMedia(media.minimalSearchString(), idsIn = mappings.map { it.remoteID }, filterOutUnnecessary = true)
            if (result.isSuccess)
                _malData.emit(result.data)
            else
                errorString = trackingErrorMessage(result.readableMessage)
        }
    }

    fun fetchMediaState(mainVm: MainDataViewModel, media: Media) = screenModelScope.launch {
        runTrackingRequest {
            val result = MALTracking.fetchMALTrackingData(media, mainVm.malApiClient, mainVm.malUser)
            _malData.emit(result.getOrThrow())
        }
    }

    fun updateRemoteStatus(mainVm: MainDataViewModel, status: CommonMediaStatus, media: Media) = screenModelScope.launch {
        runTrackingRequest {
            val result = MALTracking.updateRemoteStatus(status, mainVm.malApiClient, mainVm.malUser)
            if (result.success)
                fetchMediaState(mainVm, media).join()
            else
                errorString = trackingErrorMessage(result.message)
        }
    }

    internal suspend fun runTrackingRequest(action: suspend () -> Unit) =
        requests.run(action)
}
