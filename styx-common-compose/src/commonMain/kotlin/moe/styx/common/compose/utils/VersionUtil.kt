package moe.styx.common.compose.utils

import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.toVersionOrNull
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import moe.styx.common.http.httpClient
import moe.styx.common.json
import moe.styx.common.util.Log

suspend fun fetchVersions(url: String): List<Version> {
    return runCatching {
        val response = httpClient.get(url)
        val jsonArray = json.decodeFromString<JsonArray>(response.bodyAsText())
        val versions = mutableListOf<Version>()
        jsonArray.iterator().forEach { element ->
            val obj = element.jsonObject
            obj["name"]?.jsonPrimitive?.contentOrNull?.toVersionOrNull(false)?.let { versions.add(it) }
        }
        versions
    }.onFailure {
        Log.w(null, it) { "Could not fetch versions!" }
    }.getOrNull() ?: emptyList()
}