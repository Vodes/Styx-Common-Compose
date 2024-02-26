package moe.styx.common.compose.utils

import android.util.Log as AndroidLog

actual object Log : ALog() {
    override fun printMsg(message: String, prefix: String, source: String?, exception: Throwable?, printStack: Boolean) {
        val fallback = if (message.isBlank() && exception != null) "Exception: ${exception.message}" else message
        var msg = "${getFormattedTime()} - [$prefix] - $fallback"
        if (!source.isNullOrBlank())
            msg += "\n  at: $source"
        if (exception != null && message.isBlank())
            msg += "\n  Exception: ${exception.message}"

        when (prefix) {
            "I" -> AndroidLog.i(source, message, exception)
            "D" -> AndroidLog.d(source, message, exception)
            "W" -> AndroidLog.w(source, message, exception)
            else -> AndroidLog.e(source, message, exception)
        }

        if (printStack)
            exception?.printStackTrace()
    }
}