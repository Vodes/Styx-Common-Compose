package moe.styx.common.compose.files

import io.github.xxfast.kstore.extensions.getOrEmpty
import kotlinx.coroutines.*
import moe.styx.common.compose.appConfig
import moe.styx.common.compose.extensions.downloadFile
import moe.styx.common.compose.extensions.getPath
import moe.styx.common.compose.extensions.isCached
import moe.styx.common.extension.eqI
import moe.styx.common.util.Log
import moe.styx.common.util.SYSTEMFILES
import moe.styx.common.util.launchGlobal
import okio.Path
import okio.Path.Companion.toPath

object ImageCache {
    val cacheDir: Path by lazy {
        "${appConfig().appCachePath}/images".toPath()
    }

    fun checkForNewImages() {
        SYSTEMFILES.createDirectories(cacheDir, false)
        launchGlobal {
            val jobs = mutableSetOf<Job>()
            val images = Stores.imageStore.getOrEmpty()
            for (image in images) {
                if (image.isCached())
                    continue

                jobs.add(launch(Dispatchers.IO) {
                    runCatching {
                        image.downloadFile()
                    }.onFailure {
                        Log.e { "Failed to download image: ${image.getPath().name}" }
                    }
                })
                if (jobs.size > 4) {
                    jobs.joinAll()
                    jobs.clear()
                }
            }
            jobs.joinAll()
            jobs.clear()
        }
        deleteUnusedImages()
    }

    private fun deleteUnusedImages() {
        val files = SYSTEMFILES.listOrNull(cacheDir) ?: emptyList()
        runBlocking {
            val images = Stores.imageStore.getOrEmpty()
            for (file in files) {
                val corresponding = images.find { it.getPath().name eqI file.name }
                if (corresponding == null) {
                    runCatching {
                        SYSTEMFILES.delete(file)
                        Log.i { "Deleted unused image: ${file.name}" }
                    }.onFailure {
                        Log.e { "Failed to delete unused image: ${file.name}" }
                    }
                }
            }
        }
    }
}