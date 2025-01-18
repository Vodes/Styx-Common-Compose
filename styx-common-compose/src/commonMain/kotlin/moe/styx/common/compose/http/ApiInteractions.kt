package moe.styx.common.compose.http

import co.touchlab.stately.collections.ConcurrentMutableMap
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import moe.styx.common.compose.utils.ServerStatus
import moe.styx.common.data.ApiResponse
import moe.styx.common.extension.currentUnixSeconds
import moe.styx.common.http.httpClient
import moe.styx.common.json
import moe.styx.common.util.Log

val failedRequests = ConcurrentMutableMap<Endpoints, Int>()

/**
 * Function to receive a list of objects from an API endpoint.
 *
 * @param endpoint API Endpoint
 * @return List of objects deserialized with the type defined
 */
suspend inline fun <reified T> getList(endpoint: Endpoints): ReceiveListResult<List<T>?> {
    if (login == null || currentUnixSeconds() > login!!.tokenExpiry) {
        if (!isLoggedIn())
            return ReceiveListResult(-1, Result.failure(Exception("Not logged in.")))
    }
    Log.d { "getList Request to: ${endpoint.name}" }

    var keepTrying = true
    var response: HttpResponse? = null
    while (keepTrying) {
        response = runCatching {
            keepTrying = false
            httpClient.submitForm(
                endpoint.url(),
                formParameters = Parameters.build {
                    append("token", login!!.accessToken)
                }
            )
        }.onFailure {
            val fails = failedRequests.getOrElse(endpoint) { 0 }
            failedRequests[endpoint] = fails + 1
            if (fails < 2) {
                Log.w("getList for Endpoint $endpoint", it) { "Request Failed, retrying (${fails + 1})" }
                    .also { ServerStatus.lastKnown = ServerStatus.UNKNOWN }
                keepTrying = true
            } else {
                Log.e("getList for Endpoint $endpoint", it) { "Request Failed" }
                    .also { ServerStatus.lastKnown = ServerStatus.UNKNOWN }
            }
            return ReceiveListResult(-1, Result.failure(it))
        }.getOrNull()
    }
    if (response == null) {
        return ReceiveListResult(-1, Result.failure(Exception("Failed after retrying!")))
    }
    failedRequests[endpoint] = 0

    ServerStatus.setLastKnown(response.status)
    Log.d { "getList Request response code for ${endpoint.name}: ${response.status.value}" }

    if (response.status.value in 200..203)
        return ReceiveListResult(response.status.value, Result.success(json.decodeFromString(response.bodyAsText())))

    return ReceiveListResult(
        response.status.value,
        Result.failure(Exception("Invalid response from server: ${response.bodyAsText()}"))
    )
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
    if (endpoint != Endpoints.HEARTBEAT)
        Log.d { "sendObjectWithResponse Request to: ${endpoint.name}" }

    var keepTrying = true
    var request: HttpResponse? = null

    while (keepTrying) {
        request = runCatching {
            keepTrying = false
            httpClient.submitForm(endpoint.url(), formParameters = parameters {
                append("token", login!!.accessToken)
                append("content", json.encodeToString(data))
            })
        }.onFailure {
            val fails = failedRequests.getOrElse(endpoint) { 0 }
            failedRequests[endpoint] = fails + 1
            if (fails < 2) {
                Log.w("sendObjectWithResponse for Endpoint $endpoint", it) { "Request Failed, retrying (${fails + 1})" }
                    .also { ServerStatus.lastKnown = ServerStatus.UNKNOWN }
                keepTrying = true
            } else {
                Log.e("sendObjectWithResponse for Endpoint $endpoint", it) { "Request Failed" }
                    .also { ServerStatus.lastKnown = ServerStatus.UNKNOWN }
            }
        }.getOrNull()
    }

    if (request == null)
        return@runBlocking null

    failedRequests[endpoint] = 0

    ServerStatus.setLastKnown(request.status)

    if (endpoint != Endpoints.HEARTBEAT)
        Log.d { "sendObjectWithResponse Request response code for ${endpoint.name}: ${request.status.value}" }

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
    Log.d { "getObject Request to: ${endpoint.name}" }

    val response = runCatching {
        httpClient.get {
            url(endpoint.url())
        }
    }.onFailure {
        Log.e("getObject for Endpoint $endpoint", it) { "Request Failed" }
            .also { ServerStatus.lastKnown = ServerStatus.UNKNOWN }
    }.getOrNull() ?: return@runBlocking null

    ServerStatus.setLastKnown(response.status)

    Log.d { "getObject Request response code for ${endpoint.name}: ${response.status.value}" }

    if (response.status.value in 200..203) {
        return@runBlocking json.decodeFromString(response.bodyAsText())
    }

    return@runBlocking null
}

data class ReceiveListResult<out T>(val httpCode: Int, val result: Result<T>)