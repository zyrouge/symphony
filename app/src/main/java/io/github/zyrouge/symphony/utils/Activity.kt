package io.github.zyrouge.symphony.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

fun startBrowserActivity(activity: Context, uri: Uri) {
    activity.startActivity(Intent(Intent.ACTION_VIEW).setData(uri))
}

