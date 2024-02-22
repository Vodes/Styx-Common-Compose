package moe.styx.common.compose.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import moe.styx.common.extension.formattedStr

abstract class ALog {
    internal fun getFormattedTime() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).formattedStr()

    fun i(source: String? = null, message: () -> String) {
        printMsg(message(), "I", source)
    }

    fun e(source: String? = null, exception: Throwable? = null, printStack: Boolean = true, message: () -> String = { "" }) {
        printMsg(message(), "E", source, exception, printStack)
    }

    fun d(source: String? = null, message: () -> String) {
        printMsg(message(), "D", source)
    }

    fun w(source: String? = null, exception: Throwable? = null, message: () -> String) {
        printMsg(message(), "W", source, exception, false)
    }

    abstract fun printMsg(
        message: String,
        prefix: String,
        source: String? = null,
        exception: Throwable? = null,
        printStack: Boolean = true,
    )
}

expect object Log : ALog