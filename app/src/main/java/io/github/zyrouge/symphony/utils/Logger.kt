package io.github.zyrouge.symphony.utils

import android.util.Log
import io.github.zyrouge.symphony.services.AppMeta

object Logger {
    const val tag = "${AppMeta.appName}Logger"
    fun warn(text: String) = Log.i(tag, text)
}
