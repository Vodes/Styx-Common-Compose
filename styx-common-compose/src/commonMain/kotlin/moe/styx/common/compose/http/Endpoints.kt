package moe.styx.common.compose.http

import com.russhwolf.settings.get
import moe.styx.common.compose.AppContextImpl.appConfig
import moe.styx.common.compose.files.Stores
import moe.styx.common.compose.files.getBlocking
import moe.styx.common.compose.settings
import moe.styx.common.extension.eqI

enum class Endpoints(private val path: String) {
    LOGIN("/login"),
    LOGOUT("/logout"),
    DEVICE_CREATE("/device/create"),
    DEVICE_FIRST_AUTH("/device/firstAuth"),
    HEARTBEAT("/heartbeat"),
    PROXY_SERVERS("/proxy-servers"),

    MEDIA("/media/list"),
    MEDIA_ENTRIES("/media/entries"),
    MEDIAINFO("/media/info"),
    IMAGES("/media/images"),
    CATEGORIES("/media/categories"),
    SCHEDULES("/media/schedules"),

    MEDIA_PREFS("/media/preferences/list"),
    MEDIA_PREFS_UPDATE("/media/preferences/update"),

    MAL_TOKEN("/mal/fetch-token"),

    FAVOURITES("/favourites/list"),
    FAVOURITES_ADD("/favourites/add"),
    FAVOURITES_DELETE("/favourites/delete"),
    FAVOURITES_SYNC("/favourites/sync"),

    WATCHED("/watched/list"),
    WATCHED_ADD("/watched/add"),
    WATCHED_DELETE("/watched/delete"),
    WATCHED_SYNC("/watched/sync"),

    CHANGES("/changes"),
    WATCH("/watch"),

    MPV("/mpv"),
    MPV_DOWNLOAD("/mpv/download"),
    DOWNLOAD_BUILD_BASE("/download");

    fun url(): String {
        val default = appConfig().apiBaseURL + this.path
        val selectedServer = settings["selected-server", ""]
        if (selectedServer.isNotBlank()) {
            val proxy = Stores.proxyServerStore.getBlocking().find { it.name eqI selectedServer }
            if (proxy != null) {
                return proxy.baseURL.removeSuffix("/") + this.path
            }
        }
        return default
    }
}