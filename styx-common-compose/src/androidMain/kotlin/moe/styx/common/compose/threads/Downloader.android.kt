package moe.styx.common.compose.threads

import android.app.DownloadManager
import android.net.Uri
import io.github.xxfast.kstore.extensions.getOrEmpty
import moe.styx.common.compose.AppContextImpl
import moe.styx.common.compose.extensions.getPathAndIDFromAndroidURI
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.http.Endpoints
import moe.styx.common.compose.http.login
import moe.styx.common.data.MediaEntry
import moe.styx.common.extension.eqI
import moe.styx.common.util.Log
import moe.styx.common.util.SYSTEMFILES
import moe.styx.common.util.launchThreaded
import okio.Path
import okio.Path.Companion.toOkioPath
import java.io.File

val downloadManager: DownloadManager by lazy {
    AppContextImpl.get().getSystemService(DownloadManager::class.java)
}

actual fun addToSystemDownloaderQueue(entries: List<MediaEntry>) {
    if (login == null)
        return
    launchThreaded {
        val context = AppContextImpl.get()
        for (ent in entries) {
            val media = Storage.stores.mediaStore.getOrEmpty().find { it.GUID eqI ent.mediaID } ?: continue
            val downloadUriString = "${Endpoints.WATCH.url()}/${ent.GUID}?token=${login?.watchToken}"
            val downloadUri = runCatching { Uri.parse(downloadUriString) }.onFailure {
                Log.e("Downloader", it) {
                    "Failed to parse uri from String: $downloadUriString"
                }
            }.getOrNull()

            if (downloadUri == null) {
                Log.e { "Resulting uri ended up being null regardless!" }
                continue
            }

            val downloaderRequest = DownloadManager.Request(downloadUri)
                .setMimeType("video/x-matroska")
                .setTitle("${media.name} - ${ent.entryNumber}")
                .setDestinationInExternalFilesDir(context, null, "downloaded/${ent.GUID}.mkv")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            downloadManager.enqueue(downloaderRequest)
        }
    }
}

actual fun getDownloadPaths(): Pair<Path, Path> {
    val externalFiles = AppContextImpl.get().getExternalFilesDir(null)!!
    return File(externalFiles, "temp").toOkioPath().also { SYSTEMFILES.createDirectories(it) } to
            File(externalFiles, "downloaded").toOkioPath().also { SYSTEMFILES.createDirectories(it) }
}

private fun reset() = launchThreaded {
    DownloadQueue.currentDownload.emit(null)
    DownloadQueue.queuedEntries.emit(emptyList())
}

actual fun updateSystemDownloaderQueue() {
    val results = downloadManager.query(DownloadManager.Query())
    if (results == null) {
        reset()
        return
    }
    val statusCol = results.getColumnIndex(DownloadManager.COLUMN_STATUS)
    val uriCol = results.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)

    if (statusCol == -1 || uriCol == -1) {
        reset()
        return
    }
    val pendingEntries = mutableListOf<String>()
    var downloadingEntry: DownloadProgress? = null
    while (results.moveToNext()) {
        try {
            val uri = results.getString(uriCol) ?: continue
            val status = results.getInt(statusCol)
            if (status in arrayOf(DownloadManager.STATUS_PENDING, DownloadManager.STATUS_PAUSED, DownloadManager.STATUS_RUNNING))
                Log.d { "Downloading: $uri ($status)" }
            val (_, entryID) = uri.getPathAndIDFromAndroidURI()

            if (status in arrayOf(DownloadManager.STATUS_PENDING, DownloadManager.STATUS_PAUSED)) {
                pendingEntries.add(entryID)
            } else if (status == DownloadManager.STATUS_RUNNING) {
                val currentBytesCol = results.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                val totalBytesCol = results.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                val currentBytes = results.getLong(currentBytesCol).let { if (it <= 0L) 1L else it }
                val totalBytes = results.getLong(totalBytesCol)
                downloadingEntry = DownloadProgress(
                    entryID,
                    ((currentBytes.toDouble() / totalBytes.toDouble()) * 100).toInt()
                )
                pendingEntries.add(entryID)
            }
        } catch (ex: Exception) {
            Log.e(exception = ex) { "Failed to fetch download queue and progress!" }
        }
    }
    launchThreaded {
        DownloadQueue.currentDownload.emit(downloadingEntry)
        DownloadQueue.queuedEntries.emit(pendingEntries)
    }
}