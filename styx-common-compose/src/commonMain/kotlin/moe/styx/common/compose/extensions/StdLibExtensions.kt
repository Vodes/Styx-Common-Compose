package moe.styx.common.compose.extensions

import kotlin.math.floor
import kotlin.math.roundToInt

fun Long.readableSize(useBinary: Boolean = false): String {
    val units = if (useBinary) listOf("B", "KiB", "MiB", "GiB", "TiB") else listOf("B", "KB", "MB", "GB", "TB")
    val divisor = if (useBinary) 1024 else 1000
    var steps = 0
    var current = this.toDouble()
    while (floor((current / divisor)) > 0) {
        current = (current / divisor)
        steps++;
    }
    return "${if (steps > 2) current.roundToDecimals(1) else current.roundToDecimals(2)} ${units[steps]}"
}

fun Double.roundToDecimals(decimals: Int): Double {
    var dotAt = 1
    repeat(decimals) { dotAt *= 10 }
    val roundedValue = (this * dotAt).roundToInt()
    return (roundedValue / dotAt) + (roundedValue % dotAt).toDouble() / dotAt
}

fun String.removeSomeHTMLTags(): String {
    return this.replace("<i>", "").replace("</i>", "").replace("<b>", "").replace("</b>", "")
}

private val appendedNumberRegex = "([0-9A-Fa-f]{8}-?[0-9A-Fa-f]{4}-?[0-9A-Fa-f]{4}-?[0-9A-Fa-f]{4}-?[0-9A-Fa-f]{12})-\\d+".toRegex()

fun String.getPathAndIDFromAndroidURI(): Pair<String, String> {
    val withoutProtocol = "/${this.removePrefix("file:///").removePrefix("file://").removePrefix("file:/")}"
    var id = withoutProtocol.substringAfterLast("/").substringBeforeLast(".")
    val match = appendedNumberRegex.find(id)
    if (match != null) {
        id = match.groups[1]!!.value
    }
    return withoutProtocol to id
}