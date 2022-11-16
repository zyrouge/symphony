package io.github.zyrouge.symphony.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Size

object AndroidXShorty {
    fun startBrowserActivity(context: Context, uri: Uri) {
        context.startActivity(Intent(Intent.ACTION_VIEW).setData(uri))
    }

    fun startBrowserActivity(context: Context, url: String) =
        startBrowserActivity(context, Uri.parse(url))

    fun checkIfContentUriExists(context: Context, uri: Uri): Boolean {
        return try {
            // NOTE: this seems to be a nasty hack, none other works
            context.contentResolver.loadThumbnail(uri, Size(1, 1), null)
            true
        } catch (_: Exception) {
            false
        }
    }
}

