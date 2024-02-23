package moe.styx.common.compose.extensions

import io.github.xxfast.kstore.extensions.getOrEmpty
import kotlinx.coroutines.runBlocking
import moe.styx.common.compose.files.Storage
import moe.styx.common.data.Image
import moe.styx.common.data.Media
import moe.styx.common.extension.eqI

fun Media.isFav(): Boolean = runBlocking {
    val favs = Storage.stores.favouriteStore.getOrEmpty()
    return@runBlocking favs.find { it.mediaID eqI this@isFav.GUID } != null
}

fun Media.getThumb(): Image? = runBlocking {
    return@runBlocking Storage.imageList.find { it.GUID eqI (this@getThumb.thumbID ?: "") }
}