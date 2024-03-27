package moe.styx.common.compose.threads

import io.github.xxfast.kstore.extensions.getOrEmpty
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import moe.styx.common.Platform
import moe.styx.common.compose.appConfig
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.files.updateList
import moe.styx.common.compose.http.Endpoints
import moe.styx.common.compose.http.isLoggedIn
import moe.styx.common.compose.http.login
import moe.styx.common.compose.utils.ServerStatus
import moe.styx.common.data.MediaEntry
import moe.styx.common.extension.eqI
import moe.styx.common.http.DownloadResult
import moe.styx.common.http.downloadFileStream
import moe.styx.common.util.Log
import moe.styx.common.util.SYSTEMFILES
import moe.styx.common.util.launchGlobal
import moe.styx.common.util.launchThreaded
import okio.Path
import okio.Path.Companion.toPath

object DownloadQueue : LifecycleTrackedJob(false) {
    val currentDownload: MutableStateFlow<DownloadProgress?> = MutableStateFlow(null)
    val queuedEntries: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())

    val tempDir by lazy {
        "${appConfig().appStoragePath}/temp".toPath()
    }
    val downloadedDir by lazy {
        "${appConfig().appStoragePath}/downloaded".toPath()
    }

    override fun createJob(): Job = launchGlobal {
        while (true) {
            if (ServerStatus.lastKnown == ServerStatus.UNKNOWN || !isLoggedIn()) {
                delay(10000)
                continue
            }
            val queued = queuedEntries.value
            if (currentDownload.value == null && queued.isNotEmpty()) {
                downloadFile(queued.first())
                queuedEntries.emit(queued.filterNot { it eqI queued.first() })
            }
            delay(2000)
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

    fun addToQueue(mediaEntry: MediaEntry) = launchGlobal {
        queuedEntries.emit(queuedEntries.value.toMutableList().apply { add(mediaEntry.GUID) }.toList())
    }

    fun addToQueue(entries: List<MediaEntry>) = launchGlobal {
        queuedEntries.emit(queuedEntries.value.toMutableList().apply { entries.forEach { add(it.GUID) } }.toList())
    }

    private fun downloadFile(entryID: String) = launchThreaded {
        currentDownload.emit(DownloadProgress(entryID, 0))
        SYSTEMFILES.createDirectories(downloadedDir, false)
        SYSTEMFILES.createDirectories(tempDir, false)

        val output = tempDir / "$entryID.mkv"
        val result = downloadFileStream("${Endpoints.WATCH.url}/${entryID}?token=${login?.watchToken}", output) {
            async { currentDownload.emit(DownloadProgress(entryID, it)) }
        }
        currentDownload.emit(null)
        val metadata = SYSTEMFILES.metadataOrNull(output)
        if (result !is DownloadResult.OK || metadata == null || metadata.size == null) {
            SYSTEMFILES.delete(output, false)
            return@launchThreaded
        }
        val entry = Storage.stores.entryStore.getOrEmpty().find { it.GUID eqI entryID }
        val media = entry?.let { Storage.stores.mediaStore.getOrEmpty().find { it.GUID eqI entry.mediaID } }
        if (entry == null || media == null) {
            Log.e("Downloader") { "Somehow an unknown ep was downloaded?" }
            return@launchThreaded
        }
        if (entry.fileSize != metadata.size) {
            Log.e("Downloader") { "File downloaded does not have the correct size. Deleting." }
            SYSTEMFILES.delete(output, false)
            return@launchThreaded
        }
        val target = downloadedDir / "$entryID.mkv"
        if (SYSTEMFILES.exists(target))
            SYSTEMFILES.delete(target)
        SYSTEMFILES.atomicMove(output, target)
        Storage.stores.downloadedStore.updateList {
            it.add(DownloadedEntry(entryID, target.toString()))
        }
        Log.i("Downloader") { "Successfully downloaded '${media.name} - ${entry.entryNumber}'." }
    }

}

data class DownloadProgress(val entryID: String, val progressPercent: Int)

@Serializable
data class DownloadedEntry(val entryID: String, val path: String) {

    val okioPath: Path
        get() = path.toPath()
}