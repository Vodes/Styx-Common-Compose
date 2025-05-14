package moe.styx.common.compose.extensions

import moe.styx.common.data.MappingCollection
import moe.styx.common.data.MediaEntry
import moe.styx.common.data.MediaWatched
import moe.styx.common.data.tmdb.StackType

data class LocalRemoteMapping(val remoteID: Int, val localEntry: MediaEntry, val localWatched: MediaWatched?, val remoteNum: Int)
typealias LocalRemoteMappingGroup = Map<Int, List<LocalRemoteMapping>>

fun MappingCollection.mapLocalToRemote(
    stackType: StackType,
    entries: List<MediaEntry>,
    watched: List<MediaWatched>,
    discardNonWatched: Boolean = false
): LocalRemoteMappingGroup? {
    if (stackType == StackType.TMDB) return null
    val mappings = if (stackType == StackType.MAL) this.malMappings else this.anilistMappings
    if (mappings.isEmpty()) return null

    val fallbackMapping = mappings.find { it.matchFrom == -1.0 && it.matchUntil == -1.0 }
    var rangeMappings = mappings.filter { it.matchFrom != -1.0 && it.matchUntil != -1.0 }
    val specificEpisodeMappings = mappings.filter { it.matchFrom != -1.0 && (it.matchFrom == it.matchUntil || it.matchUntil == -1.0) }

    // Sanitize mappings that specify episode ranges for everything but where I might've forgotten to set an offset.
    if (fallbackMapping == null && rangeMappings.size > 1 && rangeMappings.all { it.offset == 0.0 }) {
        val sorted = rangeMappings.sortedBy { it.matchFrom }.toMutableList()
        sorted.forEachIndexed { i, m ->
            if (i == 0)
                return@forEachIndexed
            sorted[i].offset = -(sorted[i - 1].matchUntil)
        }
        rangeMappings = sorted.toList()
    }

    val localMappingList = mutableListOf<LocalRemoteMapping>()

    for (entry in entries) {
        val entryNum = entry.entryNumber.toDouble().let {
            // Fallback to EP 1 for movies or specials
            if (it == 0.0 && entries.size == 1)
                1.0
            else
                it
        }
        val watched = watched.find { it.entryID == entry.GUID }
        if (discardNonWatched && (watched == null || watched.maxProgress < 0.85F))
            continue
        var localMapping: LocalRemoteMapping? = null
        for (mapping in specificEpisodeMappings) {
            if (entryNum == mapping.matchFrom) {
                localMapping = LocalRemoteMapping(mapping.remoteID, entry, watched, (entryNum + mapping.offset).toInt())
                break
            }
        }
        if (localMapping == null) {
            for (mapping in rangeMappings) {
                if (entryNum >= mapping.matchFrom && entryNum <= mapping.matchUntil) {
                    localMapping = LocalRemoteMapping(mapping.remoteID, entry, watched, (entryNum + mapping.offset).toInt())
                    break
                }
            }
        }
        if (localMapping == null && fallbackMapping != null) {
            localMapping = LocalRemoteMapping(
                fallbackMapping.remoteID,
                entry,
                watched,
                (entryNum + fallbackMapping.offset).toInt()
            )
        }
        if (localMapping != null)
            localMappingList.add(localMapping)
    }
    return localMappingList.sortedBy { it.localEntry.entryNumber.toDoubleOrNull() ?: -1.0 }.groupBy { it.remoteID }
}