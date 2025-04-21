package moe.styx.common.compose.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.Painter
import io.kamel.core.Resource
import io.kamel.image.asyncPainterResource
import io.ktor.http.*
import moe.styx.common.data.Image

@Composable
actual fun Image.getPainter(): Resource<Painter> {
    return if (isCached()) {
        asyncPainterResource("file:/${getPath()}", key = GUID, filterQuality = FilterQuality.Low)
    } else
        asyncPainterResource(Url(getURL()), key = GUID, filterQuality = FilterQuality.Low)
}