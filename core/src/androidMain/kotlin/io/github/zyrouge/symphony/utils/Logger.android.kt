package io.github.zyrouge.symphony.utils

import android.util.Log

actual object Logger {
    // Matches the previous `AppMeta.appName + "Logger"` tag ("SymphonyLogger").
    private const val TAG = "SymphonyLogger"

    actual fun warn(mod: String, text: String) {
        Log.w(TAG, "$mod: $text")
    }

    actual fun warn(mod: String, text: String, throwable: Throwable) =
        warn(mod, joinTextThrowable(text, throwable))

    actual fun error(mod: String, text: String) {
        Log.e(TAG, "$mod: $text")
    }

    actual fun error(mod: String, text: String, throwable: Throwable) =
        error(mod, joinTextThrowable(text, throwable))

    actual fun joinTextThrowable(text: String, throwable: Throwable) =
        joinTextThrowableImpl(text, throwable)
}
