package moe.styx.common.compose.extensions

import io.kamel.core.config.KamelConfig
import io.kamel.core.config.httpFetcher
import io.kamel.core.config.takeFrom
import io.kamel.image.config.Default
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import moe.styx.common.Platform
import moe.styx.common.compose.appConfig
import moe.styx.common.compose.files.ImageCache
import moe.styx.common.compose.files.SYSTEMFILES
import moe.styx.common.data.Image
import moe.styx.common.extension.toBoolean
import moe.styx.common.http.httpClient
import okio.Path

val kamelConfig by lazy {
    KamelConfig {
        imageBitmapCacheSize = if (Platform.current == Platform.JVM) 120 else 100
        svgCacheSize = if (Platform.current == Platform.JVM) 75 else 50
        imageVectorCacheSize = if (Platform.current == Platform.JVM) 150 else 100
        takeFrom(KamelConfig.Default)
        httpFetcher(httpClient)
    }
}

/**
 * Extension to get the remote URL for the image.
 */
fun Image.getURL(): String {
    return if (hasWEBP?.toBoolean() == true) {
        "${appConfig().imageBaseURL}/$GUID.webp"
    } else if (hasJPG?.toBoolean() == true) {
        "${appConfig().imageBaseURL}/$GUID.jpg"
    } else if (hasPNG?.toBoolean() == true) {
        "${appConfig().imageBaseURL}/$GUID.png"
    } else {
        return externalURL as String
    }
}

/**
 * Extension to get the okio path for the image in the cacheDir.
 */
fun Image.getPath(): Path {
    return if (hasWEBP?.toBoolean() == true)
        ImageCache.cacheDir / "$GUID.webp"
    else if (hasJPG?.toBoolean() == true)
        ImageCache.cacheDir / "$GUID.jpg"
    else
        ImageCache.cacheDir / "$GUID.png"
}


/**
 * Extension to check if the file exists locally and isn't empty.
 */
fun Image.isCached(): Boolean {
    return SYSTEMFILES.exists(getPath()) && (SYSTEMFILES.metadataOrNull(getPath())?.size ?: 0) > 100
}

/**
 * Function to download the image via ktor and write it to the local cache.
 */
suspend fun Image.downloadFile() {
    val response = httpClient.get(this.getURL())
    if (response.status.isSuccess()) {
        val bytes = response.bodyAsChannel().readRemaining().readBytes()
        SYSTEMFILES.write(this.getPath()) {
            write(bytes)
        }
        bytes.fill(0)
    }
}