package io.github.zyrouge.symphony.utils

import android.util.Log
import io.github.zyrouge.symphony.services.AppMeta

object Logger {
    private const val tag = "${AppMeta.appName}Logger"

    fun warn(mod: String, text: String) = Log.w(tag, "$mod: $text")
    fun error(mod: String, text: String) = Log.e(tag, "$mod: $text")
}
