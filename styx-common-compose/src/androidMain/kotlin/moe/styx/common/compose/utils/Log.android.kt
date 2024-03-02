package moe.styx.common.compose.utils

import android.util.Log as AndroidLog

actual object Log : ALog() {
    override fun printMsg(message: String, prefix: String, source: String?, exception: Throwable?, printStack: Boolean) {
        when (prefix) {
            "I" -> AndroidLog.i(source ?: "Styx", message, exception)
            "D" -> AndroidLog.d(source ?: "Styx", message, exception)
            "W" -> AndroidLog.w(source ?: "Styx", message, exception)
            else -> AndroidLog.e(source ?: "Styx", message, exception)
        }

        if (printStack)
            exception?.printStackTrace()
    }
}