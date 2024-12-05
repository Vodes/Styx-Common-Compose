package moe.styx.common.compose.threads

import moe.styx.common.data.MediaEntry

actual fun addToSystemDownloaderQueue(entries: List<MediaEntry>) {
    throw NotImplementedError() // This stuff is android only for now, iOS may follow.
}