package io.github.zyrouge.symphony.utils

import android.util.Log

object Logger {
    const val tag = "SymLog"
    fun warn(text: String) = Log.i(tag, text)
}