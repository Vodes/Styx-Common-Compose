package moe.styx.common.compose.files

import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.extensions.getOrEmpty
import io.github.xxfast.kstore.file.extensions.listStoreOf
import io.github.xxfast.kstore.file.storeOf
import kotlinx.coroutines.runBlocking
import moe.styx.common.compose.appConfig
import moe.styx.common.compose.threads.DownloadedEntry
import moe.styx.common.compose.utils.SearchState
import moe.styx.common.data.*
import moe.styx.common.extension.currentUnixSeconds
import moe.styx.common.json
import okio.Path.Companion.toPath

/**
 * A collection of KStore's with all the data we might need.
 *
 * It's not recommended to get this object directly and use the getter [moe.styx.common.compose.files.Storage.stores] instead.
 */
object Stores {
    var lastLoaded = 0L

    val mediaStore: KStore<List<Media>> by lazy {
        listStoreOf("${appConfig().appStoragePath}/store/media.json".toPath(), json = json)
    }

    val entryStore: KStore<List<MediaEntry>> by lazy {
        listStoreOf("${appConfig().appStoragePath}/store/entries.json".toPath(), json = json)
    }

    val watchedStore: KStore<List<MediaWatched>> by lazy {
        listStoreOf("${appConfig().appStoragePath}/store/watched.json".toPath(), json = json)
    }

    val mediainfoStore: KStore<List<MediaInfo>> by lazy {
        listStoreOf("${appConfig().appStoragePath}/store/mediainfo.json".toPath(), json = json)
    }

    val categoryStore: KStore<List<Category>> by lazy {
        listStoreOf("${appConfig().appStoragePath}/store/categories.json".toPath(), json = json)
    }

    val imageStore: KStore<List<Image>> by lazy {
        listStoreOf("${appConfig().appStoragePath}/store/images.json".toPath(), json = json)
    }

    val favouriteStore: KStore<List<Favourite>> by lazy {
        listStoreOf("${appConfig().appStoragePath}/store/favourites.json".toPath(), json = json)
    }

    val scheduleStore: KStore<List<MediaSchedule>> by lazy {
        listStoreOf("${appConfig().appStoragePath}/store/schedules.json".toPath(), json = json)
    }

    val changesStore: KStore<Changes> by lazy {
        storeOf("${appConfig().appStoragePath}/store/changes.json".toPath(), Changes(0, 0), json = json)
    }

    val queuedFavStore: KStore<QueuedFavChanges> by lazy {
        storeOf("${appConfig().appStoragePath}/queued/fav-changes.json".toPath(), QueuedFavChanges(), json = json)
    }

    val queuedWatchedStore: KStore<QueuedWatchedChanges> by lazy {
        storeOf("${appConfig().appStoragePath}/queued/watched-changes.json".toPath(), QueuedWatchedChanges(), json = json)
    }

    val downloadedStore: KStore<List<DownloadedEntry>> by lazy {
        listStoreOf("${appConfig().appStoragePath}/store/downloaded.json".toPath(), json = json)
    }

    val showSearchState: KStore<SearchState> by lazy {
        storeOf("${appConfig().appStoragePath}/store/show-search.json".toPath(), SearchState(), json = json)
    }

    val movieSearchState: KStore<SearchState> by lazy {
        storeOf("${appConfig().appStoragePath}/store/movie-search.json".toPath(), SearchState(), json = json)
    }

    val favSearchState: KStore<SearchState> by lazy {
        storeOf("${appConfig().appStoragePath}/store/fav-search.json".toPath(), SearchState(), json = json)
    }

    fun needsRefresh(): Boolean = runBlocking {
        return@runBlocking mediaStore.getOrEmpty().isEmpty() || entryStore.getOrEmpty().isEmpty() || lastLoaded < (currentUnixSeconds() - 3600000)
    }
}