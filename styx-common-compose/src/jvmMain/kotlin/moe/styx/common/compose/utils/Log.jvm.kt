package moe.styx.common.compose.utils

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal

actual object Log : ALog() {
    private val terminal = Terminal()

    private fun prefixColorFromPrefix(prefix: String): TextColors? {
        return when (prefix) {
            "D" -> TextColors.brightGreen
            "E" -> TextColors.brightRed
            "W" -> TextColors.brightYellow
            "I" -> TextColors.gray
            else -> null
        }
    }

    override fun printMsg(message: String, prefix: String, source: String?, exception: Throwable?, printStack: Boolean) {
        val fallback = if (message.isBlank() && exception != null) "Exception: ${exception.message}" else message
        val prefixColor = prefixColorFromPrefix(prefix)
        var msg = if (prefixColor == null)
            "${getFormattedTime()} - [$prefix] - $fallback"
        else {
            val pre = prefixColor("${getFormattedTime()} - [$prefix]")
            "$pre - $fallback"
        }
        if (!source.isNullOrBlank())
            msg += "\n  at: $source"
        if (exception != null && message.isBlank())
            msg += "\n  Exception: ${exception.message}"

        terminal.println(msg, stderr = prefix == "E")

        if (printStack)
            exception?.printStackTrace()
    }
}