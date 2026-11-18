package io.github.zyrouge.symphony.utils

import android.graphics.Bitmap
import androidx.core.graphics.scale

object ImagePreserver {
    fun resize(bitmap: Bitmap, quality: ImageQuality): Bitmap {
        val dimensions = calculateResizedDimensions(bitmap.width, bitmap.height, quality.maxSide)
            ?: return bitmap
        return bitmap.scale(dimensions.first, dimensions.second)
    }
}
