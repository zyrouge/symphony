package io.github.zyrouge.symphony.utils

actual object Logger {
    private const val TAG = "SymphonyLogger"

    actual fun warn(mod: String, text: String) = System.err.println("$TAG/W: $mod: $text")
    actual fun warn(mod: String, text: String, throwable: Throwable) =
        warn(mod, joinTextThrowable(text, throwable))

    actual fun error(mod: String, text: String) = System.err.println("$TAG/E: $mod: $text")
    actual fun error(mod: String, text: String, throwable: Throwable) =
        error(mod, joinTextThrowable(text, throwable))

    actual fun joinTextThrowable(text: String, throwable: Throwable) =
        joinTextThrowableImpl(text, throwable)
}
