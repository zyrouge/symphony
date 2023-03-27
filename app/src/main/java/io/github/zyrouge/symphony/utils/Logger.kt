package io.github.zyrouge.symphony.utils

import android.util.Log
import io.github.zyrouge.symphony.services.AppMeta

object Logger {
    private const val tag = "${AppMeta.appName}Logger"

    fun warn(mod: String, text: String) = Log.w(tag, "$mod: $text")
    fun warn(mod: String, text: String, throwable: Throwable) =
        warn(mod, joinTextThrowable(text, throwable))

    fun error(mod: String, text: String) = Log.e(tag, "$mod: $text")
    fun error(mod: String, text: String, throwable: Throwable) =
        error(mod, joinTextThrowable(text, throwable))

    fun joinTextThrowable(text: String, throwable: Throwable) = StringBuilder().apply {
        append(text)
        append("\nError: ${throwable.message}")
        append("\nStack trace: ${throwable.stackTraceToString()}")
    }.toString()
}
