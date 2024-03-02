package moe.styx.common.compose.files

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.extensions.getOrEmpty
import io.github.xxfast.kstore.extensions.updatesOrEmpty
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import moe.styx.common.data.Changes
import moe.styx.common.data.MediaWatched
import moe.styx.common.data.QueuedFavChanges
import moe.styx.common.data.QueuedWatchedChanges
import moe.styx.common.extension.eqI

fun KStore<Changes>.getOrDefault(): Changes = runBlocking {
    return@runBlocking this@getOrDefault.get() ?: Changes(0, 0)
}

fun KStore<QueuedFavChanges>.getOrDefault(): QueuedFavChanges = runBlocking {
    return@runBlocking this@getOrDefault.get() ?: QueuedFavChanges()
}

fun KStore<QueuedWatchedChanges>.getOrDefault(): QueuedWatchedChanges = runBlocking {
    return@runBlocking this@getOrDefault.get() ?: QueuedWatchedChanges()
}

fun KStore<QueuedWatchedChanges>.addWatched(watched: MediaWatched) = runBlocking {
    this@addWatched.update { changes ->
        val current = changes ?: QueuedWatchedChanges()
        current.toUpdate.removeAll { it.entryID eqI watched.entryID }
        current.toRemove.removeAll { it.entryID eqI watched.entryID }
        current.toUpdate.add(watched)
        return@update current
    }
}

fun KStore<QueuedWatchedChanges>.removeWatched(watched: MediaWatched) = runBlocking {
    this@removeWatched.update { changes ->
        val current = changes ?: QueuedWatchedChanges()
        current.toUpdate.removeAll { it.entryID eqI watched.entryID }
        current.toRemove.removeAll { it.entryID eqI watched.entryID }
        current.toRemove.add(watched)
        return@update current
    }
}

fun <T : @Serializable Any> KStore<List<T>>.getBlocking(): List<T> = runBlocking {
    return@runBlocking this@getBlocking.getOrEmpty()
}

@Composable
inline fun <T : @Serializable Any> KStore<List<T>>.getCurrentAndCollectFlow(): State<List<T>> {
    val current = runBlocking { this@getCurrentAndCollectFlow.getOrEmpty() }
    return this.updatesOrEmpty.collectAsState(current)
}

@Composable
inline fun <T : @Serializable Any> KStore<List<T>>.collectWithEmptyInitial(): State<List<T>> {
    return this.updatesOrEmpty.collectAsState(emptyList())
}

suspend inline fun <reified T> KStore<List<T>>.updateList(crossinline block: (MutableList<T>) -> Unit) {
    this.update {
        val mutable = it?.toMutableList() ?: mutableListOf()
        block(mutable)
        mutable.toList()
    }
}