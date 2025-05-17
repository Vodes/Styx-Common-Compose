package moe.styx.common.compose.components.tracking.anilist

import io.github.xxfast.kstore.extensions.getOrEmpty
import moe.styx.common.compose.components.tracking.common.CommonMediaListStatus
import moe.styx.common.compose.components.tracking.common.CommonMediaStatus
import moe.styx.common.compose.components.tracking.common.CommonTrackingResult
import moe.styx.common.compose.extensions.mapLocalToRemote
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.viewmodels.MainDataViewModel
import moe.styx.common.compose.viewmodels.MediaStorage
import moe.styx.common.data.Media
import moe.styx.common.data.MediaWatched
import moe.styx.common.data.tmdb.StackType
import moe.styx.common.data.tmdb.decodeMapping
import pw.vodes.anilistkmp.AnilistApiClient
import pw.vodes.anilistkmp.ext.deleteMediaListEntries
import pw.vodes.anilistkmp.ext.fetchUserMediaList
import pw.vodes.anilistkmp.ext.saveMediaListEntry
import pw.vodes.anilistkmp.ext.searchMedia

object AnilistTracking {

    private fun handleClientUser(client: AnilistApiClient? = null, user: AlUser? = null): Pair<AnilistApiClient?, AlUser?> {
        return (client ?: MainDataViewModel.publicAnilistApiClient) to (user ?: MainDataViewModel.publicAnilistUser)
    }

    suspend fun updateRemoteStatus(status: CommonMediaStatus, client: AnilistApiClient? = null, user: AlUser? = null): CommonTrackingResult {
        val (anilistClient, alUser) = handleClientUser(client, user)
        if (anilistClient == null)
            return CommonTrackingResult(false, "Anilist API Client not initialized!")
        if (alUser == null)
            return CommonTrackingResult(false, "Anilist User not logged in!")

        if (status.status == CommonMediaListStatus.NONE) {
            val deleteResponse = anilistClient.deleteMediaListEntries(status.entryID)
            if (deleteResponse.data != true && (!deleteResponse.errors.isNullOrEmpty() || deleteResponse.exception != null)) {
                val errors = deleteResponse.errors?.map { it.message }?.joinToString { "\n" }
                val message = "Failed to delete media list entry from anilist"
                return CommonTrackingResult(false, "$message: $errors", deleteResponse.exception)
            }
        } else {
            val updateResponse = anilistClient.saveMediaListEntry(
                if (status.entryID != -1) status.entryID else null,
                status.mediaID,
                status = status.status.toAnilistStatus(),
                progress = if (status.progress != -1) status.progress else null
            )
            if (updateResponse.data == null && (!updateResponse.errors.isNullOrEmpty() || updateResponse.exception != null)) {
                val errors = updateResponse.errors?.map { it.message }?.joinToString { "\n" }
                val message = "Failed to update media list entry on anilist"
                return CommonTrackingResult(false, "$message: $errors", updateResponse.exception)
            }
        }
        return CommonTrackingResult(true)
    }

    suspend fun fetchAnilistTrackingData(media: Media, client: AnilistApiClient? = null, user: AlUser? = null): Result<AlTrackingData> {
        val (anilistClient, alUser) = handleClientUser(client, user)
        if (anilistClient == null)
            return Result.failure(Exception("Anilist API Client not initialized!"))

        val mappings = media.decodeMapping()
        if (mappings == null || mappings.anilistMappings.isEmpty())
            return Result.failure(Exception("No anilist mapping found for: ${media.name}"))

        val fetchedAlMedia = anilistClient.searchMedia(idIn = mappings.anilistMappings.map { it.remoteID })

        if (fetchedAlMedia.data.isEmpty()) {
            val messageString = fetchedAlMedia.errors?.map { it.message }?.joinToString { "\n" }
            return Result.failure(Exception("Failed to fetch anilist media: $messageString\n\tException: ${fetchedAlMedia.exception?.message}"))
        }
        val userAlMedia = alUser?.let { u ->
            anilistClient.fetchUserMediaList(
                userID = u.id,
                mediaIdIn = mappings.anilistMappings.map { it.remoteID })
        }

        if (userAlMedia != null && (!userAlMedia.errors.isNullOrEmpty() || userAlMedia.exception != null)) {
            val messageString = userAlMedia.errors?.map { it.message }?.joinToString { "\n" }
            return Result.failure(Exception("Failed to fetch anilist user medialist: $messageString\n\tException: ${userAlMedia.exception?.message}"))
        }

        val map = mutableMapOf<AlMedia, AlUserEntry?>()
        fetchedAlMedia.data.forEach { media ->
            map[media] = userAlMedia?.data?.find { it.media.id == media.id }
        }
        return Result.success(map.toMap())
    }

    suspend fun syncAnilistProgress(
        mediaStorage: MediaStorage,
        watchedListIn: List<MediaWatched>?,
        anilistDataIn: AlTrackingData?,
        client: AnilistApiClient? = null,
        user: AlUser? = null
    ): CommonTrackingResult {
        val media = mediaStorage.media
        var anilistData = anilistDataIn
        if (anilistDataIn == null) {
            val result = fetchAnilistTrackingData(media, client, user)
            if (result.isFailure)
                return CommonTrackingResult(false, result.exceptionOrNull()!!.message!!)
            else
                anilistData = result.getOrThrow()
        }
        var watchedList = watchedListIn
        if (watchedList == null) {
            watchedList = Storage.stores.watchedStore.getOrEmpty()
        }

        var mappingGroups = media.decodeMapping()?.mapLocalToRemote(StackType.ANILIST, mediaStorage.entries, watchedList, true)
            ?: return CommonTrackingResult(false, "Could not decode mappings or map the local watched list to the remote list!")
        mappingGroups = mappingGroups.mapValues { group ->
            val highestEntry = group.value.maxBy { it.localEntry.entryNumber.toDoubleOrNull() ?: 0.0 }
            return@mapValues listOf(highestEntry)
        }
        for (group in mappingGroups) {
            val anilistDataEntry = anilistData.entries.find { it.key.id == group.key } ?: continue

            if ((anilistDataEntry.value?.listEntry?.progress ?: 0) >= group.value.first().remoteNum)
                continue
            val knownMax = anilistDataEntry.key.episodes
            val status = CommonMediaStatus(
                anilistDataEntry.value?.listEntry?.id ?: -1,
                anilistDataEntry.key.id,
                if (knownMax != null && knownMax <= group.value.first().remoteNum) CommonMediaListStatus.COMPLETED else CommonMediaListStatus.WATCHING,
                group.value.first().remoteNum,
                knownMax ?: Int.MAX_VALUE
            )
            val result = updateRemoteStatus(status, client, user)
            if (!result.success)
                return result
        }
        return CommonTrackingResult(true)
    }
}