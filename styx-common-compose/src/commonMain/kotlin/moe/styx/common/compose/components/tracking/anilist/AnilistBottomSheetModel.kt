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
import moe.styx.common.compose.components.tracking.common.CommonMediaListStatus
import moe.styx.common.compose.components.tracking.common.CommonMediaStatus
import moe.styx.common.compose.viewmodels.MainDataViewModel
import moe.styx.common.data.Media
import moe.styx.common.data.tmdb.decodeMapping
import moe.styx.common.util.Log
import pw.vodes.anilistkmp.ext.deleteMediaListEntries
import pw.vodes.anilistkmp.ext.fetchUserMediaList
import pw.vodes.anilistkmp.ext.saveMediaListEntry
import pw.vodes.anilistkmp.ext.searchMedia


class AnilistBottomSheetModel : ScreenModel {
    var errorString by mutableStateOf<String?>(null)

    private val _anilistData = MutableStateFlow<AnilistMediaState?>(null)
    val anilistData = _anilistData.stateIn(screenModelScope, SharingStarted.WhileSubscribed(2000), null)


    fun fetchMediaState(mainVm: MainDataViewModel, media: Media) = screenModelScope.launch {
        val mappings = media.decodeMapping() ?: return@launch

        if (mappings.anilistMappings.isEmpty())
            return@launch

        val fetchedAlMedia = mainVm.anilistApiClient!!.searchMedia(idIn = mappings.anilistMappings.map { it.remoteID })
        val userAlMedia =
            mainVm.anilistUser?.let { user ->
                mainVm.anilistApiClient!!.fetchUserMediaList(
                    userID = user.id,
                    mediaIdIn = mappings.anilistMappings.map { it.remoteID })
            }

        if (fetchedAlMedia.data.isEmpty()) {
            val messageString = fetchedAlMedia.errors?.map { it.message }?.joinToString { "\n" }
            Log.e(exception = fetchedAlMedia.exception) { "Failed to fetch anilist media: $messageString" }
            _anilistData.emit(AnilistMediaState(emptyList(), errored = true))
            return@launch
        }

        if (userAlMedia != null && (!userAlMedia.errors.isNullOrEmpty() || userAlMedia.exception != null)) {
            val messageString = userAlMedia.errors?.map { it.message }?.joinToString { "\n" }
            Log.e(exception = userAlMedia.exception) { "Failed to fetch anilist user medialist: $messageString" }
            _anilistData.emit(AnilistMediaState(fetchedAlMedia.data, null, true))
            return@launch
        }
        _anilistData.emit(AnilistMediaState(fetchedAlMedia.data, userAlMedia?.data, false))
    }

    fun updateRemoteStatus(mainVm: MainDataViewModel, status: CommonMediaStatus, media: Media) = screenModelScope.launch {
        var updateData = true
        if (status.status == CommonMediaListStatus.NONE) {
            val deleteResponse = mainVm.anilistApiClient!!.deleteMediaListEntries(status.entryID)
            if (deleteResponse.data != true && (!deleteResponse.errors.isNullOrEmpty() || deleteResponse.exception != null)) {
                updateData = false
                val errors = deleteResponse.errors?.map { it.message }?.joinToString { "\n" }
                val message = "Failed to delete media list entry from anilist"
                Log.e(exception = deleteResponse.exception) { "$message: $errors" }
                errorString = "$message!"
            }
        } else {
            val updateResponse = mainVm.anilistApiClient!!.saveMediaListEntry(
                if (status.entryID != -1) status.entryID else null,
                status.mediaID,
                status = status.status.toAnilistStatus(),
                progress = if (status.progress != -1) status.progress else null
            )
            if (updateResponse.data == null && (!updateResponse.errors.isNullOrEmpty() || updateResponse.exception != null)) {
                updateData = false
                val errors = updateResponse.errors?.map { it.message }?.joinToString { "\n" }
                val message = "Failed to update media list entry on anilist"
                Log.e(exception = updateResponse.exception) { "$message: $errors" }
                errorString = "$message!"
            }
        }

        if (updateData)
            fetchMediaState(mainVm, media)
    }
}

data class AnilistMediaState(val alMedia: AlMediaList, val userMedia: AlUserMediaEntries? = null, val errored: Boolean = false)