package moe.styx.common.compose.threads

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import moe.styx.common.Platform
import moe.styx.common.compose.files.*
import moe.styx.common.compose.http.Endpoints
import moe.styx.common.compose.http.isLoggedIn
import moe.styx.common.compose.http.login
import moe.styx.common.compose.http.sendObject
import moe.styx.common.compose.utils.Log
import moe.styx.common.compose.utils.ServerStatus
import moe.styx.common.data.*
import moe.styx.common.extension.currentUnixSeconds
import moe.styx.common.extension.eqI
import moe.styx.common.extension.replaceIfNotNull
import moe.styx.common.util.launchGlobal
import moe.styx.common.util.launchThreaded

object RequestQueue : LifecycleTrackedJob() {

    override fun createJob(): Job = launchGlobal {
        delay(3000)
        while (runJob) {
            if (ServerStatus.lastKnown == ServerStatus.UNKNOWN || !isLoggedIn()) {
                delay(30000L)
                continue
            }
            val favStore = Storage.stores.queuedFavStore.getOrDefault()
            if (favStore.toAdd.isNotEmpty() || favStore.toRemove.isNotEmpty()) {
                syncFavs(favStore)
            }
            val watchedStore = Storage.stores.queuedWatchedStore.getOrDefault()
            if (watchedStore.toUpdate.isNotEmpty() || watchedStore.toRemove.isNotEmpty()) {
                syncWatched(watchedStore)
            }
            delay(15000L)
        }
    }

    private suspend fun syncFavs(favs: QueuedFavChanges) {
        if (ServerStatus.lastKnown != ServerStatus.UNKNOWN && isLoggedIn()
            && sendObject(Endpoints.FAVOURITES_SYNC, favs)
        ) {
            Storage.stores.queuedFavStore.set(QueuedFavChanges())
            Log.i { "Synced queued up favourites" }
        }
    }

    private suspend fun syncWatched(watched: QueuedWatchedChanges) {
        if (ServerStatus.lastKnown != ServerStatus.UNKNOWN && isLoggedIn()
            && sendObject(Endpoints.WATCHED_SYNC, watched)
        ) {
            Storage.stores.queuedWatchedStore.set(QueuedWatchedChanges())
            Log.i { "Synced queued up watch progress" }
        }
    }

    fun start() {
        if (Platform.current != Platform.JVM) {
            Log.w("RequestQueue::start") { "This function is not designed for use outside of Desktop applications." }
            return
        }
        runJob = true
        currentJob = createJob()
    }

    fun addFav(media: Media): Job? {
        val favs = Storage.stores.favouriteStore.getBlocking()
        val existing = favs.find { it.mediaID eqI media.GUID }
        if (existing != null)
            return null
        val fav = Favourite(media.GUID, login?.userID ?: "", currentUnixSeconds())

        return launchThreaded {
            Storage.stores.favouriteStore.updateList { it.add(fav) }
            Storage.stores.queuedFavStore.update { changes ->
                val current = changes ?: QueuedFavChanges()
                current.toAdd.removeAll { it.mediaID eqI media.GUID }
                current.toRemove.removeAll { it.mediaID eqI media.GUID }
                return@update current
            }
            if (ServerStatus.lastKnown == ServerStatus.UNKNOWN || !isLoggedIn() || !sendObject(Endpoints.FAVOURITES_ADD, fav)) {
                Storage.stores.queuedFavStore.update { changes ->
                    val current = changes ?: QueuedFavChanges()
                    current.toAdd.add(fav)
                    return@update current
                }
            }
        }
    }

    fun removeFav(media: Media): Job? {
        val favs = Storage.stores.favouriteStore.getBlocking()
        val fav = favs.find { it.mediaID eqI media.GUID } ?: return null
        return launchThreaded {
            Storage.stores.favouriteStore.updateList { it.remove(fav) }
            if (ServerStatus.lastKnown == ServerStatus.UNKNOWN || !isLoggedIn() || !sendObject(Endpoints.FAVOURITES_DELETE, fav)) {
                Storage.stores.queuedFavStore.update { changes ->
                    val current = changes ?: QueuedFavChanges()
                    current.toRemove.add(fav)
                    return@update current
                }
            }
        }
    }

    fun updateWatched(mediaWatched: MediaWatched): Job {
        val existingList = Storage.stores.watchedStore.getBlocking()
        val existing = existingList.find { it.entryID eqI mediaWatched.entryID }
        val existingMax = existing?.maxProgress ?: -1F
        val new = if (existingMax > mediaWatched.maxProgress) mediaWatched.copy(maxProgress = existingMax) else mediaWatched
        return launchThreaded {
            Storage.stores.watchedStore.updateList { it.replaceIfNotNull(existing, new) }
            Storage.stores.queuedWatchedStore.update { changes ->
                val current = changes ?: QueuedWatchedChanges()
                current.toUpdate.removeAll { it.entryID eqI mediaWatched.entryID }
                current.toRemove.removeAll { it.entryID eqI mediaWatched.entryID }
                return@update current
            }
            if (ServerStatus.lastKnown == ServerStatus.UNKNOWN || !isLoggedIn() || !sendObject(Endpoints.WATCHED_ADD, new))
                Storage.stores.queuedWatchedStore.addWatched(new)
        }
    }

    fun addMultipleWatched(entries: List<MediaEntry>): Job {
        val existingList = Storage.stores.watchedStore.getBlocking()
        val now = currentUnixSeconds()
        return launchThreaded {
            Storage.stores.watchedStore.updateList { watched ->
                entries.forEach { entry ->
                    val existing = existingList.find { it.entryID eqI entry.GUID }
                    val new = MediaWatched(entry.GUID, login?.userID ?: "", now, 0, 0F, 100F)
                    watched.replaceIfNotNull(existing, new)
                    Storage.stores.queuedWatchedStore.addWatched(new)
                }
            }
        }
    }

    fun removeMultipleWatched(entries: List<MediaEntry>): Job? {
        val existingList = Storage.stores.watchedStore.getBlocking()
        return launchThreaded {
            Storage.stores.watchedStore.updateList { watched ->
                entries.forEach { entry ->
                    val existing = existingList.find { it.entryID eqI entry.GUID } ?: return@forEach
                    watched.remove(existing)
                    Storage.stores.queuedWatchedStore.removeWatched(existing)
                }
            }
        }
    }

    fun removeWatched(entry: MediaEntry): Job? {
        val existingList = Storage.stores.watchedStore.getBlocking()
        val existing = existingList.find { it.entryID eqI entry.GUID } ?: return null
        return launchThreaded {
            Storage.stores.watchedStore.updateList { it.remove(existing) }
            Storage.stores.queuedWatchedStore.update { changes ->
                val current = changes ?: QueuedWatchedChanges()
                current.toUpdate.removeAll { it.entryID eqI entry.GUID }
                current.toRemove.removeAll { it.entryID eqI entry.GUID }
                return@update current
            }
            if (ServerStatus.lastKnown == ServerStatus.UNKNOWN || !isLoggedIn() || !sendObject(Endpoints.WATCHED_DELETE, existing))
                Storage.stores.queuedWatchedStore.removeWatched(existing)
        }
    }
}