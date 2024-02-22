package moe.styx.common.compose.extensions

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import moe.styx.common.compose.appConfig
import moe.styx.common.compose.files.ImageCache
import moe.styx.common.compose.files.SYSTEMFILES
import moe.styx.common.data.Image
import moe.styx.common.extension.toBoolean
import moe.styx.common.http.httpClient
import okio.Path

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

fun Image.getPath(): Path {
    return if (hasWEBP?.toBoolean() == true)
        ImageCache.cacheDir / "$GUID.webp"
    else if (hasJPG?.toBoolean() == true)
        ImageCache.cacheDir / "$GUID.jpg"
    else
        ImageCache.cacheDir / "$GUID.png"
}

fun Image.isCached(): Boolean {
    return SYSTEMFILES.exists(getPath()) && (SYSTEMFILES.metadataOrNull(getPath())?.size ?: 0) > 0
}

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