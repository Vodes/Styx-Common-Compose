package moe.styx.common.compose

import com.russhwolf.settings.Settings

expect val settings: Settings

/**
 * Just a BuildConfig wrapper type thing, so we don't have to use BuildConfig in this library.
 */
data class AppConfig(val appSecret: String, val appVersion: String, val apiBaseURL: String, val debugToken: String? = null)

/**
 * Change the function in this variable in your app.
 */
var appConfig: () -> AppConfig = {
    AppConfig("", "", "")
}