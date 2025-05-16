package moe.styx.common.compose.components.tracking.anilist

import moe.styx.common.compose.components.tracking.common.CommonMediaListStatus
import moe.styx.common.compose.components.tracking.common.CommonMediaStatus
import moe.styx.common.compose.extensions.mapLocalToRemote
import moe.styx.common.data.MappingCollection
import moe.styx.common.data.Media
import moe.styx.common.data.MediaEntry
import moe.styx.common.data.MediaWatched
import moe.styx.common.data.tmdb.StackType
import moe.styx.common.data.tmdb.decodeMapping
import pw.vodes.anilistkmp.AnilistApiClient
import pw.vodes.anilistkmp.ApolloResponse
import pw.vodes.anilistkmp.MediaListEntry
import pw.vodes.anilistkmp.ext.fetchUserMediaList
import pw.vodes.anilistkmp.ext.searchMedia
import pw.vodes.anilistkmp.graphql.fragment.MediaBig

fun syncAnilistProgress(
    media: Media,
    anilistData: List<Pair<MediaBig, MediaListEntry?>>,
    entries: List<MediaEntry>,
    watched: List<MediaWatched>,
    updateFunction: (CommonMediaStatus) -> Unit
) {
    var mappingGroups = media.decodeMapping()?.mapLocalToRemote(StackType.ANILIST, entries, watched, true) ?: return
    mappingGroups = mappingGroups.mapValues { group ->
        val highestEntry = group.value.maxBy { it.localEntry.entryNumber.toDoubleOrNull() ?: 0.0 }
        return@mapValues listOf(highestEntry)
    }
    for (group in mappingGroups) {
        val anilistDataEntry = anilistData.find { it.first.id == group.key } ?: continue
        if ((anilistDataEntry.second?.listEntry?.progress ?: 0) >= group.value.first().remoteNum)
            continue
        val knownMax = anilistDataEntry.first.episodes
        val status = CommonMediaStatus(
            anilistDataEntry.second?.listEntry?.id ?: -1,
            anilistDataEntry.first.id,
            if (knownMax != null && knownMax <= group.value.first().remoteNum) CommonMediaListStatus.COMPLETED else CommonMediaListStatus.WATCHING,
            group.value.first().remoteNum,
            knownMax ?: Int.MAX_VALUE
        )
        updateFunction(status)
    }
}

suspend fun fetchAnilistDataForMapping(
    mappings: MappingCollection,
    client: AnilistApiClient,
    user: AlUser?
): Pair<ApolloResponse<List<MediaBig>>, ApolloResponse<List<MediaListEntry>>?> {
    val fetchedAlMedia = client.searchMedia(idIn = mappings.anilistMappings.map { it.remoteID })
    val userAlMedia =
        user?.let { user ->
            client.fetchUserMediaList(
                userID = user.id,
                mediaIdIn = mappings.anilistMappings.map { it.remoteID })
        }
    return fetchedAlMedia to userAlMedia
}