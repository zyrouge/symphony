package io.github.zyrouge.symphony.utils

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import io.github.zyrouge.symphony.Symphony

class AndroidXShorty(val symphony: Symphony) {
    fun startBrowserActivity(activity: Context, url: String) =
        startBrowserActivity(activity, Uri.parse(url))

    fun startBrowserActivity(activity: Context, uri: Uri) {
        activity.startActivity(Intent(Intent.ACTION_VIEW).setData(uri))
    }

    fun checkIfMediaStoreThumbnailExists(uri: Uri): Boolean {
        return try {
            // NOTE: this seems to be a nasty hack, none other works
            symphony.applicationContext.contentResolver.openTypedAssetFile(
                uri,
                "image/*",
                Bundle(1).apply {
                    putParcelable(ContentResolver.EXTRA_SIZE, Point(1, 1))
                },
                null
            ).use { true }
        } catch (err: Exception) {
            false
        }
    }
}

