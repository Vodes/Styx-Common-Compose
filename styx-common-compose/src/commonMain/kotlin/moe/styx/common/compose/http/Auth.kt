package moe.styx.common.compose.http

import com.russhwolf.settings.get
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import moe.styx.common.compose.appConfig
import moe.styx.common.compose.settings
import moe.styx.common.compose.utils.ServerStatus
import moe.styx.common.compose.utils.ServerStatus.Companion.setLastKnown
import moe.styx.common.compose.utils.fetchDeviceInfo
import moe.styx.common.data.LoginResponse
import moe.styx.common.http.httpClient
import moe.styx.common.json

var login: LoginResponse? = null

fun isLoggedIn(): Boolean {
    val token = if (!appConfig().debugToken.isNullOrBlank()) {
        appConfig().debugToken!!
    } else {
        settings["refreshToken", ""]
    }

    if (token.isBlank()) {
        ServerStatus.lastKnown = ServerStatus.UNAUTHORIZED
        return false
    }

    if (login != null)
        return true

    val loginTry = checkLogin(token)
    if (loginTry != null) {
        login = loginTry
        return true
    }

    return false
}

fun checkLogin(token: String, first: Boolean = false): LoginResponse? = runBlocking {
    val response = runCatching {
        httpClient.submitForm(
            (if (first) Endpoints.DEVICE_FIRST_AUTH else Endpoints.LOGIN).url,
            formParameters = Parameters.build {
                append("token", token)
                append("content", runCatching { json.encodeToString(fetchDeviceInfo()) }.onFailure { it.printStackTrace() }.getOrNull() ?: "")
            }
        )
    }.onFailure { it.printStackTrace().also { ServerStatus.lastKnown = ServerStatus.UNKNOWN } }.getOrNull() ?: return@runBlocking null

    setLastKnown(response.status)

    if (response.status.value in 200..203) {
        val log = json.decodeFromString<LoginResponse>(response.bodyAsText())
        login = log
        if (first && log.refreshToken != null)
            settings.putString("refreshToken", log.refreshToken!!)
        return@runBlocking login
    } else {
        println(response.bodyAsText())
    }
    return@runBlocking null
}