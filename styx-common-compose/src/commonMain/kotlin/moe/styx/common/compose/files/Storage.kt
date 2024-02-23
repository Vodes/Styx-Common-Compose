package moe.styx.common.compose.files

import io.github.xxfast.kstore.extensions.getOrEmpty
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import moe.styx.common.compose.http.Endpoints
import moe.styx.common.compose.http.getList
import moe.styx.common.compose.http.getObject
import moe.styx.common.compose.utils.ServerStatus
import moe.styx.common.data.*
import moe.styx.common.extension.currentUnixSeconds
import okio.FileSystem

/**
 * Object for all the basic data storage operations, e.g. refreshing the data from the API.
 */
object Storage {
    /**
     * This property getter ensures presence of data in the stores.
     */
    val stores: Stores
        get() {
            if (Stores.needsRefresh()) {
                runBlocking { loadData() }
            }
            return Stores
        }

    val loadingProgress = MutableStateFlow("")

    private suspend fun loadData() = coroutineScope {
        loadingProgress.emit("")
        val serverOnline = ServerStatus.lastKnown == ServerStatus.ONLINE
        val lastChanges = (if (serverOnline) getObject<Changes>(Endpoints.CHANGES) else null) ?: Changes(0, 0)

        val localChange = Stores.changesStore.getOrDefault()
        val shouldUpdateMedia = lastChanges.media > localChange.media
        val shouldUpdateEntries = lastChanges.entry > localChange.entry

        if (serverOnline) {
            loadingProgress.emit("Loading media...")
            val jobs = mutableSetOf(
                launch { Stores.scheduleStore.set(getList(Endpoints.SCHEDULES)) },
                launch { Stores.categoryStore.set(getList(Endpoints.CATEGORIES)) },
                launch { Stores.favouriteStore.set(getList(Endpoints.FAVOURITES)) },
                launch { Stores.watchedStore.set(getList(Endpoints.WATCHED)) }
            )
            if (shouldUpdateMedia || shouldUpdateEntries) {
                jobs.add(launch { Stores.imageStore.set(getList(Endpoints.IMAGES)) })
                jobs.add(launch { Stores.mediainfoStore.set(getList(Endpoints.MEDIAINFO)) })
                if (shouldUpdateMedia)
                    jobs.add(launch { Stores.mediaStore.set(getList(Endpoints.MEDIA)) })
                if (shouldUpdateEntries)
                    jobs.add(launch { Stores.entryStore.set(getList(Endpoints.MEDIA_ENTRIES)) })
            }
            jobs.joinAll()
            val current = currentUnixSeconds()
            Stores.lastLoaded = current
            Stores.changesStore.set(
                localChange.copy(
                    if (shouldUpdateMedia) current else localChange.media,
                    if (shouldUpdateEntries) current else localChange.entry
                )
            )
            loadingProgress.emit("Updating image cache...\nThis may take a minute or two.")
            ImageCache.checkForNewImages()
        }
    }

    val mediaList: List<Media> = runBlocking { stores.mediaStore.getOrEmpty() }
    val entryList: List<MediaEntry> = runBlocking { stores.entryStore.getOrEmpty() }
    val categories: List<Category> = runBlocking { stores.categoryStore.getOrEmpty() }
    val favourites: List<Favourite> = runBlocking { stores.favouriteStore.getOrEmpty() }
    val imageList: List<Image> = runBlocking { stores.imageStore.getOrEmpty() }
    val watchedList: List<MediaWatched> = runBlocking { stores.watchedStore.getOrEmpty() }
    val schedules: List<MediaSchedule> = runBlocking { stores.scheduleStore.getOrEmpty() }
    val mediaInfos: List<MediaInfo> = runBlocking { stores.mediainfoStore.getOrEmpty() }
}

expect val SYSTEMFILES: FileSystem