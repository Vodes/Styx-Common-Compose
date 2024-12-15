package moe.styx.common.compose.threads

import moe.styx.common.data.MediaEntry
import okio.Path

actual fun addToSystemDownloaderQueue(entries: List<MediaEntry>) {
    throw NotImplementedError() // This stuff is android only for now, iOS may follow.
}

actual fun getDownloadPaths(): Pair<Path, Path> {
    TODO("Not yet implemented")
}

actual fun updateSystemDownloaderQueue() {
}