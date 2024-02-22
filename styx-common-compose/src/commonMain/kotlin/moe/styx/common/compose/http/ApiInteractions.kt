package moe.styx.common.compose.http

import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import moe.styx.common.compose.utils.Log
import moe.styx.common.compose.utils.ServerStatus
import moe.styx.common.data.ApiResponse
import moe.styx.common.extension.currentUnixSeconds
import moe.styx.common.http.httpClient
import moe.styx.common.json

/**
 * Function to receive a list of objects from an API endpoint.
 *
 * @param endpoint API Endpoint
 * @return List of objects deserialized with the type defined
 */
suspend inline fun <reified T> getList(endpoint: Endpoints): List<T> {
    if (login == null || currentUnixSeconds() > login!!.tokenExpiry) {
        if (!isLoggedIn())
            return emptyList()
    }
    val response = runCatching {
        httpClient.submitForm(
            endpoint.url,
            formParameters = Parameters.build {
                append("token", login!!.accessToken)
            }
        )
    }.onFailure {
        Log.e("getList for Endpoint $endpoint", it) { "Request Failed" }.also { ServerStatus.lastKnown = ServerStatus.UNKNOWN }
    }.getOrNull() ?: return emptyList()

    ServerStatus.setLastKnown(response.status)

    if (response.status.value in 200..203)
        return json.decodeFromString(response.bodyAsText())

    return emptyList()
}

/**
 * Function to send a json encoded object to an API endpoint and receive its response.
 *
 * @param endpoint API Endpoint
 * @param data The object to be sent
 * @return Generic ApiResponse or null
 */
inline fun <reified T> sendObjectWithResponse(endpoint: Endpoints, data: T?): ApiResponse? = runBlocking {
    if (login == null || currentUnixSeconds() > login!!.tokenExpiry) {
        if (!isLoggedIn())
            return@runBlocking null
    }
    val request = runCatching {
        httpClient.submitForm(endpoint.url, formParameters = parameters {
            append("token", login!!.accessToken)
            append("content", json.encodeToString(data))
        })
    }.onFailure {
        Log.e("sendObjectWithResponse for Endpoint $endpoint", it) { "Request Failed" }.also { ServerStatus.lastKnown = ServerStatus.UNKNOWN }
    }.getOrNull() ?: return@runBlocking null

    ServerStatus.setLastKnown(request.status)

    if (!request.status.isSuccess()) {
        val body = request.bodyAsText()
        Log.e { "Request Failed for Endpoint `$endpoint`\n${body}" }
    }

    val response = runCatching {
        json.decodeFromString<ApiResponse>(request.bodyAsText())
    }.getOrNull()

    return@runBlocking response
}

/**
 * Function to send a json encoded object to an API endpoint and check whether the response was a success.
 *
 * @param endpoint API Endpoint
 * @param data The object to be sent
 */
inline fun <reified T> sendObject(endpoint: Endpoints, data: T?): Boolean = runBlocking {
    return@runBlocking sendObjectWithResponse<T>(endpoint, data) != null
}

/**
 * Function to receive a single object from any API endpoint without auth.
 *
 * @param endpoint API Endpoint
 * @return Object deserialized with the type defined
 */
inline fun <reified T> getObject(endpoint: Endpoints): T? = runBlocking {
    if (login == null || currentUnixSeconds() > login!!.tokenExpiry) {
        if (!isLoggedIn())
            return@runBlocking null
    }
    val response = runCatching {
        httpClient.get {
            url(endpoint.url)
        }
    }.onFailure {
        Log.e("getObject for Endpoint $endpoint", it) { "Request Failed" }.also { ServerStatus.lastKnown = ServerStatus.UNKNOWN }
    }.getOrNull() ?: return@runBlocking null

    ServerStatus.setLastKnown(response.status)

    if (response.status.value in 200..203) {
        return@runBlocking json.decodeFromString(response.bodyAsText())
    }

    return@runBlocking null
}