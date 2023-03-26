package io.github.zyrouge.symphony.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import io.github.zyrouge.symphony.Symphony

class AndroidXShorty(val symphony: Symphony) {
    fun startBrowserActivity(activity: Context, url: String) =
        startBrowserActivity(activity, Uri.parse(url))

    fun startBrowserActivity(activity: Context, uri: Uri) {
        activity.startActivity(Intent(Intent.ACTION_VIEW).setData(uri))
    }
}
