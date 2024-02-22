package moe.styx.common.compose

import com.russhwolf.settings.Settings

expect val settings: Settings

/**
 * Just a BuildConfig wrapper type thing, so we don't have to use BuildConfig in this library.
 *
 * @param appSecret Secret to be sent to the API to validate if this app is valid
 * @param appVersion Current app version
 * @param apiBaseURL Base URL of the API. E.g. https://api.example.com
 * @param debugToken A refreshToken to be used when ran in debug.
 * @param appCachePath Cache path for (for now just) images.
 * @param appStoragePath Storage path for all the data.
 */
data class AppConfig(
    val appSecret: String,
    val appVersion: String = "",
    val apiBaseURL: String = "",
    val debugToken: String? = null,
    val appCachePath: String = "",
    val appStoragePath: String = ""
)

/**
 * Change the function in this variable in your app.
 */
var appConfig: () -> AppConfig = {
    AppConfig("")
}