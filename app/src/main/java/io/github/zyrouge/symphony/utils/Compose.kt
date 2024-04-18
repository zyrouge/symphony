package io.github.zyrouge.symphony.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import io.github.zyrouge.symphony.ui.helpers.ViewContext

fun <T> wrapInViewContext(fn: (ViewContext) -> T) = fn

fun copyToClipboardWithToast(context: ViewContext, text: String) {
    val clipboardManager = context.activity.getSystemService(ClipboardManager::class.java)
    clipboardManager.setPrimaryClip(ClipData.newPlainText(null, text))
    Toast.makeText(
        context.activity,
        context.symphony.t.CopiedXToClipboard(text),
        Toast.LENGTH_SHORT,
    ).show()
}
