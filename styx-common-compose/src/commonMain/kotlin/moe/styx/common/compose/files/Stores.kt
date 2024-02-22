package moe.styx.common.compose.files

import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.extensions.getOrEmpty
import io.github.xxfast.kstore.file.extensions.listStoreOf
import io.github.xxfast.kstore.file.storeOf
import kotlinx.coroutines.runBlocking
import moe.styx.common.data.*
import moe.styx.common.json
import okio.Path.Companion.toPath

/**
 * A collection of KStore's with all the data we might need.
 *
 * It's not recommended to get this object directly and use the getter [moe.styx.common.compose.files.Storage.stores] instead.
 */
object Stores {

    val mediaStore: KStore<List<Media>> by lazy {
        listStoreOf("$appStorage/store/media.json".toPath(), json = json)
    }

    val entryStore: KStore<List<MediaEntry>> by lazy {
        listStoreOf("$appStorage/store/entries.json".toPath(), json = json)
    }

    val watchedStore: KStore<List<MediaWatched>> by lazy {
        listStoreOf("$appStorage/store/watched.json".toPath(), json = json)
    }

    val mediainfoStore: KStore<List<MediaInfo>> by lazy {
        listStoreOf("$appStorage/store/mediainfo.json".toPath(), json = json)
    }

    val categoryStore: KStore<List<Category>> by lazy {
        listStoreOf("$appStorage/store/categories.json".toPath(), json = json)
    }

    val imageStore: KStore<List<Image>> by lazy {
        listStoreOf("$appStorage/store/images.json".toPath(), json = json)
    }

    val favouriteStore: KStore<List<Favourite>> by lazy {
        listStoreOf("$appStorage/store/favourites.json".toPath(), json = json)
    }

    val scheduleStore: KStore<List<MediaSchedule>> by lazy {
        listStoreOf("$appStorage/store/schedules.json".toPath(), json = json)
    }

    val changesStore: KStore<Changes> by lazy {
        storeOf("$appStorage/store/changes.json".toPath(), Changes(0, 0), json = json)
    }

    val queuedFavStore: KStore<QueuedFavChanges> by lazy {
        storeOf("$appStorage/queued/fav-changes.json".toPath(), QueuedFavChanges(), json = json)
    }

    val queuedWatchedStore: KStore<QueuedWatchedChanges> by lazy {
        storeOf("$appStorage/queued/watched-changes.json".toPath(), QueuedWatchedChanges(), json = json)
    }

    fun needsRefresh(): Boolean = runBlocking {
        return@runBlocking mediaStore.getOrEmpty().isEmpty() || entryStore.getOrEmpty().isEmpty()
    }
}