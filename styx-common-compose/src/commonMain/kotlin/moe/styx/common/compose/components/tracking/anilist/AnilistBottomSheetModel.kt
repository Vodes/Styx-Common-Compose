package moe.styx.common.compose.components.tracking.anilist

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
import moe.styx.common.compose.viewmodels.MainDataViewModel
import moe.styx.common.data.Media


class AnilistBottomSheetModel : ScreenModel {
    var errorString by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)

    private val _anilistData = MutableStateFlow<AlTrackingData?>(null)
    val anilistData = _anilistData.stateIn(screenModelScope, SharingStarted.WhileSubscribed(2000), null)


    fun fetchMediaState(mainVm: MainDataViewModel, media: Media) = screenModelScope.launch {
        isLoading = true
        val result = AnilistTracking.fetchAnilistTrackingData(media, mainVm.anilistApiClient, mainVm.anilistUser)
        if (result.isFailure) {
            errorString = result.exceptionOrNull()!!.message!!.substringBefore(":") + "!"
        } else
            _anilistData.emit(result.getOrThrow())
        isLoading = false
    }

    fun updateRemoteStatus(mainVm: MainDataViewModel, status: CommonMediaStatus, media: Media) = screenModelScope.launch {
        isLoading = true
        val result = AnilistTracking.updateRemoteStatus(status, mainVm.anilistApiClient, mainVm.anilistUser)
        if (result.success)
            fetchMediaState(mainVm, media).join()
        else
            errorString = result.message!!.substringBefore(":") + "!"
        isLoading = false
    }
}