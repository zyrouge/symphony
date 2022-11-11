package io.github.zyrouge.symphony.ui.helpers

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
