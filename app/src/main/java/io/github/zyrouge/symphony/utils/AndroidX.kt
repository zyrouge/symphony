package io.github.zyrouge.symphony.utils

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri

object AndroidXShorty {
    fun startBrowserActivity(context: Context, uri: Uri) {
        context.startActivity(Intent(Intent.ACTION_VIEW).setData(uri))
    }

    fun startBrowserActivity(context: Context, url: String) =
        startBrowserActivity(context, Uri.parse(url))
}

fun ContentResolver.exists(uri: Uri): Boolean {
    return query(uri, null, null, null)
        ?.use { true }
        ?: false
}
