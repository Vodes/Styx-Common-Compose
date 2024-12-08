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
import moe.styx.common.util.Log
import moe.styx.common.util.launchThreaded

val downloadManager: DownloadManager by lazy {
    AppContextImpl.get().getSystemService(DownloadManager::class.java)
}

actual fun addToSystemDownloaderQueue(entries: List<MediaEntry>) {
    if (login == null)
        return
    launchThreaded {
        for (ent in entries) {
            val media = Storage.stores.mediaStore.getOrEmpty().find { it.GUID eqI ent.mediaID } ?: continue
            val downloadUriString = "${Endpoints.WATCH.url()}/${ent.GUID}?token=${login?.watchToken}"
            val downloadUri = runCatching { Uri.parse(downloadUriString) }.onFailure {
                Log.e("Downloader", it) {
                    "Failed to parse uri from String: $downloadUriString"
                }
            }.getOrNull()
            val fileOutUriString = "file:/${DownloadQueue.tempDir.toString().removeSuffix("/")}/${ent.GUID}.mkv"
            val fileOutUri = runCatching { Uri.parse(fileOutUriString) }.onFailure {
                Log.e("Downloader", it) {
                    "Failed to parse uri from String: $fileOutUriString"
                }
            }.getOrNull()

            if (fileOutUri == null || downloadUri == null) {
                Log.e {
                    "Resulting uri ended up being null regardless!\n" +
                            "FileOut: ${fileOutUri.toString()} | DownloadUri: ${downloadUri.toString()}"
                }
                continue
            }

            val downloaderRequest = DownloadManager.Request(downloadUri)
                .setMimeType("video/x-matroska")
                .setTitle("$media - ${ent.entryNumber}")
                .setDestinationUri(fileOutUri)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            downloadManager.enqueue(downloaderRequest)
        }
    }
}