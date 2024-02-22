package moe.styx.common.compose.files

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import moe.styx.common.compose.http.Endpoints
import moe.styx.common.compose.http.getObject
import moe.styx.common.compose.utils.ServerStatus
import moe.styx.common.data.Changes

/**
 * The App "Library" path, aka. the data we need every time and don't want exposed to the user.
 *
 * This should be set by the app's main constructor depending on the platform.
 */
var appStorage: String? = ""

/**
 * The App cache path.
 *
 * Mostly intended to be used for the image cache.
 *
 * This should be set by the app's main constructor depending on the platform.
 */
var appCache: String? = ""

/**
 * Object for all the basic data storage operations, e.g. refreshing the data from the API.
 */
object Storage {
    /**
     * This property getter ensures presence of data in the stores.
     */
    val stores: Stores
        get() {
            if (Stores.needsRefresh()) {
                runBlocking { loadData() }
            }
            return Stores
        }

    val loadingProgress = MutableStateFlow("")

    private suspend fun loadData() = coroutineScope {
        loadingProgress.emit("")
        val serverOnline = ServerStatus.lastKnown == ServerStatus.ONLINE
        var lastChanges = if (serverOnline) getObject<Changes>(Endpoints.CHANGES) else Changes(0, 0)
        if (lastChanges == null)
            lastChanges = Changes(0, 0)
    }
}