package moe.styx.common.compose.files

import io.github.xxfast.kstore.KStore
import kotlinx.coroutines.runBlocking
import moe.styx.common.data.Changes
import moe.styx.common.data.QueuedFavChanges
import moe.styx.common.data.QueuedWatchedChanges

fun KStore<Changes>.getOrDefault(): Changes = runBlocking {
    return@runBlocking this@getOrDefault.get() ?: Changes(0, 0)
}

fun KStore<QueuedFavChanges>.getOrDefault(): QueuedFavChanges = runBlocking {
    return@runBlocking this@getOrDefault.get() ?: QueuedFavChanges()
}

fun KStore<QueuedWatchedChanges>.getOrDefault(): QueuedWatchedChanges = runBlocking {
    return@runBlocking this@getOrDefault.get() ?: QueuedWatchedChanges()
}