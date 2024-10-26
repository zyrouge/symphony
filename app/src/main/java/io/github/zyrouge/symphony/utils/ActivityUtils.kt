package io.github.zyrouge.symphony.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import io.github.zyrouge.symphony.Symphony

object ActivityUtils {
    fun startBrowserActivity(activity: Context, uri: Uri) {
        activity.startActivity(Intent(Intent.ACTION_VIEW).setData(uri))
    }

    fun copyToClipboardAndNotify(symphony: Symphony, text: String) {
        val context = symphony.applicationContext
        val clipboardManager = context.getSystemService(ClipboardManager::class.java)
        clipboardManager.setPrimaryClip(ClipData.newPlainText(null, text))
        Toast.makeText(context, symphony.t.CopiedXToClipboard(text), Toast.LENGTH_SHORT).show()
    }

    fun makePersistableReadableUri(context: Context, uri: Uri) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }

    fun releasePersistableReadableUri(context: Context, uri: Uri) {
        context.contentResolver.releasePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }
}
