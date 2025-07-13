package moe.styx.common.compose.components.tracking.mal

import io.github.xxfast.kstore.extensions.getOrEmpty
import kotlinx.coroutines.delay
import moe.styx.common.compose.components.tracking.common.CommonMediaListStatus
import moe.styx.common.compose.components.tracking.common.CommonMediaStatus
import moe.styx.common.compose.components.tracking.common.CommonTrackingResult
import moe.styx.common.compose.extensions.mapLocalToRemote
import moe.styx.common.compose.extensions.minimalSearchString
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.http.MalApiClient
import moe.styx.common.compose.viewmodels.MainDataViewModel
import moe.styx.common.compose.viewmodels.MediaStorage
import moe.styx.common.data.Media
import moe.styx.common.data.MediaWatched
import moe.styx.common.data.tmdb.StackType
import moe.styx.common.data.tmdb.decodeMapping
import moe.styx.libs.mal.ext.fetching.fetchMediaDetails
import moe.styx.libs.mal.ext.fetching.searchMedia
import moe.styx.libs.mal.ext.update.deleteMediaListEntry
import moe.styx.libs.mal.ext.update.saveMediaListEntry
import moe.styx.libs.mal.types.MALMedia
import moe.styx.libs.mal.types.MALUser
import kotlin.math.roundToInt

object MALTracking {
    private fun handleClientUser(client: MalApiClient? = null, user: MALUser? = null): Pair<MalApiClient?, MALUser?> {
        return (client ?: MainDataViewModel.publicMalApiClient) to (user ?: MainDataViewModel.publicMalUser)
    }

    suspend fun updateRemoteStatus(status: CommonMediaStatus, client: MalApiClient? = null, user: MALUser? = null): CommonTrackingResult {
        val (malClient, malUser) = handleClientUser(client, user)
        if (malClient == null || malUser == null) {
            return CommonTrackingResult(false, "MyAnimeList client was not authorized!")
        }
        if (status.status == CommonMediaListStatus.NONE) {
            val deleteResponse = malClient.deleteMediaListEntry(status.mediaID)
            if (!deleteResponse)
                return CommonTrackingResult(false, "Failed to delete media list entry from MAL!")
        } else {
            val converted = status.status.toMalStatus()
            if (converted == null) {
                return CommonTrackingResult(false, "Unknown media status!")
            }
            val updateResponse = malClient.saveMediaListEntry(
                status.mediaID,
                converted.first,
                if (status.progress != -1) status.progress else 0,
                score = status.score?.roundToInt(),
                isRewatching = converted.second
            )
            if (!updateResponse.isSuccess)
                return CommonTrackingResult(false, "Failed to update media list entry on MAL!")
        }
        return CommonTrackingResult(true, "Updated MAL status for ${status.mediaID} to ${status.progress} / ${status.knownMax} (${status.status})!")
    }

    suspend fun fetchMALTrackingData(media: Media, client: MalApiClient? = null, user: MALUser? = null): Result<List<MALMedia>> {
        val (malClient, malUser) = handleClientUser(client, user)
        if (malClient == null || malUser == null) {
            return Result.failure(Exception("MyAnimeList client was not authorized!"))
        }
        val mappings = media.decodeMapping()
        if (mappings == null || mappings.malMappings.isEmpty())
            return Result.failure(Exception("No MAL mapping found for: ${media.name}"))

        if (mappings.malMappings.size == 1) {
            val fetchResp = malClient.fetchMediaDetails(mappings.malMappings.first().remoteID)
            if (fetchResp.data == null || !fetchResp.isSuccess)
                return Result.failure(Exception("Failed to fetch MAL media! (${fetchResp.returnCode.value})"))
            return Result.success(listOf(fetchResp.data!!))
        } else {
            val fetchResp =
                malClient.searchMedia(media.minimalSearchString(), idsIn = mappings.malMappings.map { it.remoteID }, filterOutUnnecessary = true)
            if (fetchResp.data.isEmpty()) {
                return Result.failure(Exception("Failed to fetch MAL media! (${fetchResp.returnCode.value})"))
            }
            return Result.success(fetchResp.data)
        }
    }

    suspend fun syncMALProgress(
        mediaStorage: MediaStorage,
        watchedListIn: List<MediaWatched>?,
        malDataIn: List<MALMedia>?,
        client: MalApiClient? = null,
        user: MALUser? = null
    ): CommonTrackingResult {
        val media = mediaStorage.media
        var malData = malDataIn
        if (malData == null) {
            val result = fetchMALTrackingData(media, client, user)
            if (result.isFailure)
                return CommonTrackingResult(false, result.exceptionOrNull()!!.message)
            else
                malData = result.getOrThrow()
        }
        var watchedList = watchedListIn
        if (watchedList == null) {
            watchedList = Storage.stores.watchedStore.getOrEmpty()
        }

        var mappingGroups =
            media.decodeMapping()?.mapLocalToRemote(StackType.MAL, mediaStorage.entries, watchedList, true) ?: return CommonTrackingResult(
                false,
                "Could not decode mappings or map the local watched list to the remote list!"
            )
        mappingGroups = mappingGroups.mapValues { group ->
            val highestEntry = group.value.maxBy { it.localEntry.entryNumber.toDoubleOrNull() ?: 0.0 }
            return@mapValues listOf(highestEntry)
        }
        for (group in mappingGroups) {
            val malDataEntry = malData.find { it.id == group.key } ?: continue
            if ((malDataEntry.listStatus?.watchedEpisodes ?: 0) > group.value.first().remoteNum)
                continue

            val knownMax = malDataEntry.numEpisodes

            val listStatus = if (knownMax != null && knownMax > 0 && knownMax <= group.value.first().remoteNum) CommonMediaListStatus.COMPLETED
            else if (malDataEntry.listStatus?.isRewatching == true) CommonMediaListStatus.REPEATING else CommonMediaListStatus.WATCHING

            val status = CommonMediaStatus(
                -1,
                malDataEntry.id,
                status = listStatus,
                progress = group.value.first().remoteNum,
                knownMax = knownMax ?: Int.MAX_VALUE,
                score = malDataEntry.listStatus?.score?.toFloat()
            )
            val result = updateRemoteStatus(status, client, user)
            if (!result.success)
                return result

            if (mappingGroups.size > 1)
                delay(750)
        }
        return CommonTrackingResult(true)
    }
}