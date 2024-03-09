package moe.styx.common.compose.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.Painter
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.component.setupDefaultComponents
import com.seiko.imageloader.intercept.bitmapMemoryCacheConfig
import com.seiko.imageloader.intercept.imageMemoryCacheConfig
import com.seiko.imageloader.intercept.painterMemoryCacheConfig
import io.kamel.core.Resource
import io.kamel.image.asyncPainterResource
import io.ktor.http.*
import moe.styx.common.data.Image

actual fun getImageLoader(): ImageLoader {
    return ImageLoader {
        components {
            setupDefaultComponents()
        }
        interceptor {
            // cache 32MB bitmap
            bitmapMemoryCacheConfig {
                maxSize(32 * 1024 * 1024) // 32MB
            }
            // cache 50 image
            imageMemoryCacheConfig {
                maxSize(50)
            }
            // cache 50 painter
            painterMemoryCacheConfig {
                maxSize(50)
            }
        }
    }
}

@Composable
actual fun Image.getPainter(): Resource<Painter> {
    return if (isCached()) {
        asyncPainterResource(getPath().toFile(), key = GUID, filterQuality = FilterQuality.Low)
    } else
        asyncPainterResource(Url(getURL()), key = GUID, filterQuality = FilterQuality.Low)
}