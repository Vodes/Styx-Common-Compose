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

suspend fun fetchVersions(url: String): List<Version> {
    val response = httpClient.get(url)
    val jsonArray = json.decodeFromString<JsonArray>(response.bodyAsText())
    val versions = mutableListOf<Version>()
    jsonArray.iterator().forEach { element ->
        val obj = element.jsonObject
        obj["name"]?.jsonPrimitive?.contentOrNull?.toVersionOrNull(false)?.let { versions.add(it) }
    }
    return versions
}