package moe.styx.common.compose.threads

import android.app.DownloadManager
import android.net.Uri
import io.github.xxfast.kstore.extensions.getOrEmpty
import moe.styx.common.compose.AppContextImpl
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.http.Endpoints
import moe.styx.common.compose.http.login
import moe.styx.common.data.MediaEntry
import moe.styx.common.extension.eqI
import moe.styx.common.util.launchThreaded

val downloadManager by lazy {
    AppContextImpl.get().getSystemService(DownloadManager::class.java)
}

actual fun addToSystemDownloaderQueue(entries: List<MediaEntry>) {
    if (login == null)
        return
    launchThreaded {
        for (ent in entries) {
            val media = Storage.stores.mediaStore.getOrEmpty().find { it.GUID eqI ent.mediaID } ?: continue
            val downloaderRequest = DownloadManager.Request(Uri.parse("${Endpoints.WATCH.url}/${ent.GUID}?token=${login?.watchToken}"))
                .setMimeType("video/x-matroska")
                .setTitle("$media - ${ent.entryNumber}")
                .setDestinationUri(Uri.parse(DownloadQueue.tempDir.toString()))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            downloadManager.enqueue(downloaderRequest)
        }
    }
}