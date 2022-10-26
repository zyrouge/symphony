package io.github.zyrouge.symphony.ui.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.github.zyrouge.symphony.R

object Assets {
    fun getPlaceholder(context: Context): Bitmap =
        BitmapFactory.decodeResource(context.resources, R.raw.placeholder)
}
