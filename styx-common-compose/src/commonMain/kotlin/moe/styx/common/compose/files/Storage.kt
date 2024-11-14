package moe.styx.common.compose.files

import io.github.xxfast.kstore.extensions.getOrEmpty
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import moe.styx.common.compose.appConfig
import moe.styx.common.compose.http.Endpoints
import moe.styx.common.compose.http.getList
import moe.styx.common.compose.http.getObject
import moe.styx.common.compose.threads.DownloadQueue
import moe.styx.common.compose.utils.ServerStatus
import moe.styx.common.data.Changes
import moe.styx.common.data.Media
import moe.styx.common.data.MediaEntry
import moe.styx.common.extension.currentUnixSeconds
import moe.styx.common.extension.eqI
import moe.styx.common.util.SYSTEMFILES
import moe.styx.common.util.launchGlobal
import moe.styx.common.util.launchThreaded
import okio.Path.Companion.toPath
import kotlin.math.abs

/**
 * Object for all the basic data storage operations, e.g. refreshing the data from the API.
 */
object Storage {
    var refreshDataJob: Job? = null

    /**
     * This property getter ensures presence of data in the stores.
     */
    val stores: Stores
        get() {
            if (refreshDataJob == null || refreshDataJob!!.isCompleted)
                refreshDataJob = launchGlobal {
                    if (Stores.needsRefresh()) {
                        runBlocking { loadData() }
                    }
                }
            return Stores
        }

    val loadingProgress = MutableStateFlow("")
    val isLoaded = MutableStateFlow(false)

    suspend fun loadData(runPreImages: () -> Unit = {}) = coroutineScope {
        // This is a bad workaround to avoid insane amounts of reads and requests
        if (abs(Stores.lastLoaded - currentUnixSeconds()) < 2)
            if (Stores.loadBuffer > 1)
                return@coroutineScope
            else
                Stores.loadBuffer++
        else
            Stores.loadBuffer = 0

        createDirectories()
        isLoaded.emit(false)
        loadingProgress.emit("")
        val serverOnline = ServerStatus.lastKnown == ServerStatus.ONLINE
        val lastChanges = (if (serverOnline) getObject<Changes>(Endpoints.CHANGES) else null) ?: Changes(0, 0)

        val localChange = Stores.changesStore.getOrDefault()
        val shouldUpdateMedia = lastChanges.media > localChange.media
        val shouldUpdateEntries = lastChanges.entry > localChange.entry

        val dataLoaderDispatcher = Dispatchers.IO.limitedParallelism(100, "Storage DataLoader")

        if (serverOnline) {
            loadingProgress.emit("Loading media...")
            var (mediaFailed, entriesFailed) = false to false
            val jobs = mutableSetOf(
                launch(dataLoaderDispatcher) { Stores.scheduleStore.updateFromEndpoint(Endpoints.SCHEDULES) },
                launch(dataLoaderDispatcher) { Stores.categoryStore.updateFromEndpoint(Endpoints.CATEGORIES) },
                launch(dataLoaderDispatcher) { Stores.favouriteStore.updateFromEndpoint(Endpoints.FAVOURITES) },
                launch(dataLoaderDispatcher) { Stores.watchedStore.updateFromEndpoint(Endpoints.WATCHED) }
            )
            if (shouldUpdateMedia || shouldUpdateEntries) {
                jobs.add(launch(dataLoaderDispatcher) { Stores.imageStore.updateFromEndpoint(Endpoints.IMAGES) })
                jobs.add(launch(dataLoaderDispatcher) { Stores.mediainfoStore.updateFromEndpoint(Endpoints.MEDIAINFO) })
                if (shouldUpdateMedia)
                    jobs.add(launch(dataLoaderDispatcher) {
                        val mediaResult = getList<Media>(Endpoints.MEDIA)
                        if (mediaResult.httpCode !in 200..203 || mediaResult.result.isFailure)
                            mediaFailed = true
                        else
                            runCatching { Stores.mediaStore.set(mediaResult.result.getOrNull()!!) }.onFailure {
                                mediaFailed = true
                            }
                    })
                if (shouldUpdateEntries)
                    jobs.add(launch(dataLoaderDispatcher) {
                        val entryResult = getList<MediaEntry>(Endpoints.MEDIA_ENTRIES)
                        if (entryResult.httpCode !in 200..203 || entryResult.result.isFailure)
                            entriesFailed = true
                        else
                            runCatching { Stores.entryStore.set(entryResult.result.getOrNull()!!) }.onFailure {
                                entriesFailed = true
                            }
                    })
            }
            jobs.joinAll()
            val current = currentUnixSeconds()
            Stores.lastLoaded = current
            Stores.changesStore.set(
                localChange.copy(
                    if (mediaFailed) 0 else (if (shouldUpdateMedia) current else localChange.media),
                    if (entriesFailed) 0 else (if (shouldUpdateEntries) current else localChange.entry)
                )
            )
            runPreImages()
            loadingProgress.emit("Updating image cache...\nThis may take a minute or two.")
            if (!Stores.mediaStore.get().isNullOrEmpty()) {
                ImageCache.checkForNewImages()
                if (!Stores.entryStore.get().isNullOrEmpty())
                    deleteFilesForDeletedEntries()
            }
        }
        isLoaded.emit(true)
    }

    private fun deleteFilesForDeletedEntries() = launchThreaded {
        val entries = stores.entryStore.getOrEmpty()
        if (SYSTEMFILES.exists(DownloadQueue.downloadedDir)) {
            for (file in SYSTEMFILES.listRecursively(DownloadQueue.downloadedDir)) {
                val nameWithoutExt = file.name.substringBeforeLast(".")
                val correspondingEntry = entries.find { it.GUID eqI nameWithoutExt }
                if (correspondingEntry == null)
                    SYSTEMFILES.delete(file)
            }
        }
    }

    private fun createDirectories() {
        SYSTEMFILES.createDirectories("${appConfig().appStoragePath}/store".toPath())
        SYSTEMFILES.createDirectories("${appConfig().appStoragePath}/queued".toPath())
    }
}