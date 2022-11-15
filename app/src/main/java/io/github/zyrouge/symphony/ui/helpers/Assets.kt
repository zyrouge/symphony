package io.github.zyrouge.symphony.ui.helpers

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.net.Uri
import io.github.zyrouge.symphony.R

object Assets {
    fun getPlaceholderUri(context: Context) = buildUriOfResource(
        context.resources,
        R.raw.placeholder,
    )

    fun getPlaceholderBitmap(context: Context) = BitmapFactory.decodeResource(
        context.resources,
        R.raw.placeholder,
    )

    private fun buildUriOfResource(resources: Resources, resourceId: Int): Uri {
        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(resourceId))
            .appendPath(resources.getResourceTypeName(resourceId))
            .appendPath(resources.getResourceEntryName(resourceId))
            .build()
    }

}
