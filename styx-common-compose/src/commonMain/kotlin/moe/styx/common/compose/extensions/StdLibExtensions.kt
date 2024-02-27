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