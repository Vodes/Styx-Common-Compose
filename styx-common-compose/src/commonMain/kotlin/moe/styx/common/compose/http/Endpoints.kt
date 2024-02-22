package moe.styx.common.compose.http

import moe.styx.common.compose.appConfig

enum class Endpoints(private val path: String) {
    LOGIN("/login"),
    LOGOUT("/logout"),
    DEVICE_CREATE("/device/create"),
    DEVICE_FIRST_AUTH("/device/firstAuth"),
    HEARTBEAT("/heartbeat"),

    MEDIA("/media/list"),
    MEDIA_ENTRIES("/media/entries"),
    MEDIAINFO("/media/info"),
    IMAGES("/media/images"),
    CATEGORIES("/media/categories"),
    SCHEDULES("/media/schedules"),

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
    MPV_DOWNLOAD("/mpv/download");

    val url: String
        get() = appConfig().apiBaseURL + this.path
}