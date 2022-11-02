package io.github.zyrouge.symphony.ui.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.github.zyrouge.symphony.R

object Assets {
    private var cachedPlaceholder: Bitmap? = null

    fun getPlaceholder(context: Context): Bitmap {
        return cachedPlaceholder ?: run {
            cachedPlaceholder = BitmapFactory.decodeResource(context.resources, R.raw.placeholder)
            cachedPlaceholder!!
        }
    }
}
