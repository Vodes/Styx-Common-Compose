package moe.styx.common.compose.utils

actual object Log : ALog() {
    override fun printMsg(message: String, prefix: String, source: String?, exception: Throwable?, printStack: Boolean) {
        val fallback = if (message.isBlank() && exception != null) "Exception: ${exception.message}" else message
        var msg = "${getFormattedTime()} - [$prefix] - $fallback"
        if (!source.isNullOrBlank())
            msg += "\n  at: $source"
        if (exception != null && message.isBlank())
            msg += "\n  Exception: ${exception.message}"

        println(msg)

        if (printStack)
            exception?.printStackTrace()
    }
}