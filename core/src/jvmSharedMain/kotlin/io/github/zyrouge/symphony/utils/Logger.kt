package io.github.zyrouge.symphony.utils

expect object Logger {
    fun warn(mod: String, text: String)
    fun warn(mod: String, text: String, throwable: Throwable)
    fun error(mod: String, text: String)
    fun error(mod: String, text: String, throwable: Throwable)
    fun joinTextThrowable(text: String, throwable: Throwable): String
}

internal fun joinTextThrowableImpl(text: String, throwable: Throwable) = StringBuilder().apply {
    append(text)
    append("\nError: ${throwable.message}")
    append("\nStack trace: ${throwable.stackTraceToString()}")
}.toString()
