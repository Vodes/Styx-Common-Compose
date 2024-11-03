package moe.styx.common.compose.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import com.seiko.imageloader.ImageLoader
import io.kamel.core.Resource
import io.kamel.core.config.KamelConfig
import io.kamel.core.config.httpUrlFetcher
import io.kamel.core.config.takeFrom
import io.kamel.image.config.Default
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.io.readByteArray
import moe.styx.common.Platform
import moe.styx.common.compose.appConfig
import moe.styx.common.compose.files.ImageCache
import moe.styx.common.data.Image
import moe.styx.common.extension.toBoolean
import moe.styx.common.http.httpClient
import moe.styx.common.util.SYSTEMFILES
import okio.Path

val kamelConfig by lazy {
    KamelConfig {
        imageBitmapCacheSize = if (Platform.current == Platform.JVM) 400 else 300
        svgCacheSize = if (Platform.current == Platform.JVM) 100 else 75
        imageVectorCacheSize = if (Platform.current == Platform.JVM) 275 else 200
        takeFrom(KamelConfig.Default)
        httpUrlFetcher(httpClient)
    }
}

expect fun getImageLoader(): ImageLoader

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

@Composable
expect fun Image.getPainter(): Resource<Painter>


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
        val bytes = response.bodyAsChannel().readRemaining().readByteArray()
        SYSTEMFILES.write(this.getPath()) {
            write(bytes)
        }
        bytes.fill(0)
    }
}