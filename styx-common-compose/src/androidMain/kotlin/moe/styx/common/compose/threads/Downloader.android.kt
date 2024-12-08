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
                .setDestinationInExternalFilesDir(context, null, "temp/${ent.GUID}.mkv")
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