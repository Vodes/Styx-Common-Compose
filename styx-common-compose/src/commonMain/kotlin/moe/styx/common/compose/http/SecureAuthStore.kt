package moe.styx.common.compose.http

import com.russhwolf.settings.get
import moe.styx.common.compose.settings
import moe.styx.common.util.Log

private const val REFRESH_TOKEN_KEY = "styx.refresh_token"

expect object SecureAuthStore {
    suspend fun getString(key: String, default: String = ""): String
    suspend fun putString(key: String, value: String)
}

suspend fun loadRefreshToken(): String {
    val secureToken = runCatching {
        SecureAuthStore.getString(REFRESH_TOKEN_KEY, "")
    }.onFailure {
        Log.e(exception = it) { "Failed to read refresh token from secure storage." }
    }.getOrDefault("")

    if (secureToken.isNotBlank()) {
        return secureToken
    }

    val legacyToken = settings["refreshToken", ""]
    if (legacyToken.isNotBlank()) {
        runCatching {
            SecureAuthStore.putString(REFRESH_TOKEN_KEY, legacyToken)
            settings.putString("refreshToken", "")
        }.onFailure {
            Log.e(exception = it) { "Failed to migrate refresh token into secure storage." }
        }
    }

    return legacyToken
}

suspend fun saveRefreshToken(token: String) {
    runCatching {
        SecureAuthStore.putString(REFRESH_TOKEN_KEY, token)
        settings.putString("refreshToken", "")
    }.onFailure {
        Log.e(exception = it) { "Failed to write refresh token to secure storage; falling back to Settings." }
        settings.putString("refreshToken", token)
    }
}
