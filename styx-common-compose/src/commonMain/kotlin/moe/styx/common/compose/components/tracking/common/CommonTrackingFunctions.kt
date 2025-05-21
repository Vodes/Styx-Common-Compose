package moe.styx.common.compose.components.tracking.common

import com.russhwolf.settings.get
import kotlinx.coroutines.flow.first
import moe.styx.common.compose.components.tracking.anilist.AnilistTracking
import moe.styx.common.compose.http.login
import moe.styx.common.compose.settings
import moe.styx.common.compose.utils.ServerStatus
import moe.styx.common.compose.viewmodels.MainDataViewModel

object CommonTrackingFunctions {
    suspend fun syncProgressForEntry(entryID: String, mainDataViewModel: MainDataViewModel, auto: Boolean) {
        if (login == null || ServerStatus.lastKnown == ServerStatus.UNKNOWN || login?.anilistData == null) {
            return
        }
        val storage = mainDataViewModel.storageFlow.first()
        val (_, mediaStorage) = mainDataViewModel.getMediaStorageForEntryID(entryID, storage)
        val autoSyncEnabled = mediaStorage.preferences?.autoSyncEnabled ?: settings["auto-sync", false]
        if (!autoSyncEnabled && auto)
            return

        AnilistTracking.syncAnilistProgress(mediaStorage, storage.watchedList, null)
    }
}