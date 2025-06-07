package moe.styx.common.compose.components.tracking.mal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import moe.styx.common.compose.components.tracking.common.CommonMediaStatus
import moe.styx.common.compose.extensions.minimalSearchString
import moe.styx.common.compose.viewmodels.MainDataViewModel
import moe.styx.common.data.Media
import moe.styx.common.data.tmdb.decodeMapping
import moe.styx.libs.mal.JikanApiClient
import moe.styx.libs.mal.types.MALMedia

class MALBottomSheetModel : ScreenModel {
    var errorString by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)

    private val _malData = MutableStateFlow<List<MALMedia>?>(null)
    val malData = _malData.stateIn(screenModelScope, SharingStarted.WhileSubscribed(2000), null)

    fun fetchMediaStateNonAuth(media: Media) = screenModelScope.launch {
        val mappings = media.decodeMapping()?.malMappings
        if (mappings.isNullOrEmpty()) {
            errorString = "No MyAnimeList ids found for: ${media.name}"
            return@launch
        }
        isLoading = true
        val result = JikanApiClient.searchMedia(media.minimalSearchString(), idsIn = mappings.map { it.remoteID }, filterOutUnnecessary = true)
        if (result.isSuccess)
            _malData.emit(result.data)
        else
            errorString = result.readableMessage
        isLoading = false
    }

    fun fetchMediaState(mainVm: MainDataViewModel, media: Media) = screenModelScope.launch {
        isLoading = true
        val result = MALTracking.fetchMALTrackingData(media, mainVm.malApiClient, mainVm.malUser)
        if (result.isFailure) {
            errorString = result.exceptionOrNull()?.message
        } else
            _malData.emit(result.getOrThrow())
        isLoading = false
    }

    fun updateRemoteStatus(mainVm: MainDataViewModel, status: CommonMediaStatus, media: Media) = screenModelScope.launch {
        isLoading = true
        val result = MALTracking.updateRemoteStatus(status, mainVm.malApiClient, mainVm.malUser)
        if (result.success)
            fetchMediaState(mainVm, media).join()
        else
            errorString = result.message
        isLoading = false
    }
}