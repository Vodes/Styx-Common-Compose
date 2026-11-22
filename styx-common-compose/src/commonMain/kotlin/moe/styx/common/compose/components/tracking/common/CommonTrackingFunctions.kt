package moe.styx.common.compose.components.tracking.common

import com.russhwolf.settings.get
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import moe.styx.common.compose.components.tracking.anilist.AnilistTracking
import moe.styx.common.compose.components.tracking.mal.MALTracking
import moe.styx.common.compose.http.login
import moe.styx.common.compose.settings
import moe.styx.common.compose.utils.ServerStatus
import moe.styx.common.compose.viewmodels.MainDataViewModel
import moe.styx.common.util.Log

object CommonTrackingFunctions {
    suspend fun syncProgressForEntry(entryID: String, mainDataViewModel: MainDataViewModel, auto: Boolean) {
        if (login == null || ServerStatus.lastKnown == ServerStatus.UNKNOWN || (login?.anilistData == null && login?.malData == null)) {
            return
        }
        val storage = mainDataViewModel.storageFlow.first()
        val (_, mediaStorage) = mainDataViewModel.getMediaStorageForEntryID(entryID, storage)
        val autoSyncEnabled = mediaStorage.preferences?.autoSyncEnabled ?: settings["auto-sync", false]
        if (!autoSyncEnabled && auto)
            return

        if (login?.anilistData != null) {
            syncProvider("AniList") { AnilistTracking.syncAnilistProgress(mediaStorage, storage.watchedList, null) }
        }
        if (login?.malData != null) {
            syncProvider("MyAnimeList") { MALTracking.syncMALProgress(mediaStorage, storage.watchedList, null) }
        }
    }

    private suspend fun syncProvider(provider: String, sync: suspend () -> CommonTrackingResult) {
        try {
            val result = sync()
            if (!result.success) {
                Log.e(exception = result.exception) { "Failed to sync $provider: ${result.message ?: "Unknown error"}" }
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.e(exception = exception) { "Failed to sync $provider" }
        }
    }
}
