package moe.styx.common.compose.files

import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.extensions.getOrEmpty
import io.github.xxfast.kstore.file.extensions.listStoreOf
import io.github.xxfast.kstore.file.storeOf
import kotlinx.io.files.Path
import moe.styx.common.compose.AppContextImpl.appConfig
import moe.styx.common.compose.threads.DownloadedEntry
import moe.styx.common.compose.utils.SearchState
import moe.styx.common.data.*
import moe.styx.common.extension.currentUnixSeconds
import moe.styx.common.json

/**
 * A collection of KStore's with all the data we might need.
 *
 * It's not recommended to get this object directly and use the getter [moe.styx.common.compose.files.Storage.stores] instead.
 */
object Stores {
    var lastLoaded = 0L
    var loadBuffer = 0

    val mediaStore: KStore<List<Media>> by lazy {
        listStoreOf(Path("${appConfig().appStoragePath}/store/media.json"), json = json)
    }

    val entryStore: KStore<List<MediaEntry>> by lazy {
        listStoreOf(Path("${appConfig().appStoragePath}/store/entries.json"), json = json)
    }

    val watchedStore: KStore<List<MediaWatched>> by lazy {
        listStoreOf(Path("${appConfig().appStoragePath}/store/watched.json"), json = json)
    }

    val mediainfoStore: KStore<List<MediaInfo>> by lazy {
        listStoreOf(Path("${appConfig().appStoragePath}/store/mediainfo.json"), json = json)
    }

    val categoryStore: KStore<List<Category>> by lazy {
        listStoreOf(Path("${appConfig().appStoragePath}/store/categories.json"), json = json)
    }

    val imageStore: KStore<List<Image>> by lazy {
        listStoreOf(Path("${appConfig().appStoragePath}/store/images.json"), json = json)
    }

    val favouriteStore: KStore<List<Favourite>> by lazy {
        listStoreOf(Path("${appConfig().appStoragePath}/store/favourites.json"), json = json)
    }

    val scheduleStore: KStore<List<MediaSchedule>> by lazy {
        listStoreOf(Path("${appConfig().appStoragePath}/store/schedules.json"), json = json)
    }

    val changesStore: KStore<Changes> by lazy {
        storeOf(Path("${appConfig().appStoragePath}/store/changes.json"), Changes(0, 0), json = json)
    }

    val queuedFavStore: KStore<QueuedFavChanges> by lazy {
        storeOf(Path("${appConfig().appStoragePath}/queued/fav-changes.json"), QueuedFavChanges(), json = json)
    }

    val queuedWatchedStore: KStore<QueuedWatchedChanges> by lazy {
        storeOf(Path("${appConfig().appStoragePath}/queued/watched-changes.json"), QueuedWatchedChanges(), json = json)
    }

    val downloadedStore: KStore<List<DownloadedEntry>> by lazy {
        listStoreOf(Path("${appConfig().appStoragePath}/store/downloaded.json"), json = json)
    }

    val showSearchState: KStore<SearchState> by lazy {
        storeOf(Path("${appConfig().appStoragePath}/store/show-search.json"), SearchState(), json = json)
    }

    val movieSearchState: KStore<SearchState> by lazy {
        storeOf(Path("${appConfig().appStoragePath}/store/movie-search.json"), SearchState(), json = json)
    }

    val favSearchState: KStore<SearchState> by lazy {
        storeOf(Path("${appConfig().appStoragePath}/store/fav-search.json"), SearchState(), json = json)
    }

    suspend fun needsRefresh(): Boolean {
        return mediaStore.getOrEmpty().isEmpty() || entryStore.getOrEmpty().isEmpty() || lastLoaded < (currentUnixSeconds() - 3600000)
    }
}