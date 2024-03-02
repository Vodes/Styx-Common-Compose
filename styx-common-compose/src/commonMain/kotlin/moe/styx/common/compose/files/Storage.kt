package moe.styx.common.compose.files

import io.github.xxfast.kstore.extensions.getOrEmpty
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import moe.styx.common.compose.appConfig
import moe.styx.common.compose.http.Endpoints
import moe.styx.common.compose.http.getList
import moe.styx.common.compose.http.getObject
import moe.styx.common.compose.utils.ServerStatus
import moe.styx.common.data.*
import moe.styx.common.extension.currentUnixSeconds
import moe.styx.common.util.launchGlobal
import okio.FileSystem
import okio.Path.Companion.toPath

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
    val isLoaded = MutableStateFlow(false)

    suspend fun loadData() = coroutineScope {
        createDirectories()
        isLoaded.emit(false)
        loadingProgress.emit("")
        val serverOnline = ServerStatus.lastKnown == ServerStatus.ONLINE
        val lastChanges = (if (serverOnline) getObject<Changes>(Endpoints.CHANGES) else null) ?: Changes(0, 0)

        val localChange = Stores.changesStore.getOrDefault()
        val shouldUpdateMedia = lastChanges.media > localChange.media
        val shouldUpdateEntries = lastChanges.entry > localChange.entry

        if (serverOnline) {
            loadingProgress.emit("Loading media...")
            val jobs = mutableSetOf(
                launchGlobal { Stores.scheduleStore.set(getList(Endpoints.SCHEDULES)) },
                launchGlobal { Stores.categoryStore.set(getList(Endpoints.CATEGORIES)) },
                launchGlobal { Stores.favouriteStore.set(getList(Endpoints.FAVOURITES)) },
                launchGlobal { Stores.watchedStore.set(getList(Endpoints.WATCHED)) }
            )
            if (shouldUpdateMedia || shouldUpdateEntries) {
                jobs.add(launch(Dispatchers.IO) { Stores.imageStore.set(getList(Endpoints.IMAGES)) })
                jobs.add(launch(Dispatchers.IO) { Stores.mediainfoStore.set(getList(Endpoints.MEDIAINFO)) })
                if (shouldUpdateMedia)
                    jobs.add(launch(Dispatchers.IO) { Stores.mediaStore.set(getList(Endpoints.MEDIA)) })
                if (shouldUpdateEntries)
                    jobs.add(launch(Dispatchers.IO) { Stores.entryStore.set(getList(Endpoints.MEDIA_ENTRIES)) })
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
        isLoaded.emit(true)
    }

    private fun createDirectories() {
        SYSTEMFILES.createDirectories("${appConfig().appStoragePath}/store".toPath())
        SYSTEMFILES.createDirectories("${appConfig().appStoragePath}/queued".toPath())
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