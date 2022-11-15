package io.github.zyrouge.symphony.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore

object AndroidXShorty {
    fun startBrowserActivity(context: Context, uri: Uri) {
        context.startActivity(Intent(Intent.ACTION_VIEW).setData(uri))
    }

    fun startBrowserActivity(context: Context, url: String) =
        startBrowserActivity(context, Uri.parse(url))

    // NOTE: this seems to be a nasty hack
    fun checkIfContentUriExists(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.query(
                uri,
                listOf(MediaStore.MediaColumns._ID).toTypedArray(),
                null,
                null,
            ).use { cursor ->
                cursor?.moveToFirst()
                return cursor?.getString(0)?.isNotEmpty() ?: false
            }
        } catch (_: Exception) {
            false
        }
    }
}

