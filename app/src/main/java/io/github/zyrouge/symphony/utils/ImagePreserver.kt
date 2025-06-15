package io.github.zyrouge.symphony.utils

import android.graphics.Bitmap
import kotlin.math.max

object ImagePreserver {
    enum class Quality(val maxSide: Int?) {
        Low(256),
        Medium(512),
        High(1024),
        Lossless(null),
    }

    fun resize(bitmap: Bitmap, quality: Quality): Bitmap {
        if (quality.maxSide == null || max(bitmap.width, bitmap.height) < quality.maxSide) {
            return bitmap
        }
        val (width, height) = calculateDimensions(bitmap.width, bitmap.height, quality.maxSide)
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    private fun calculateDimensions(width: Int, height: Int, maxSide: Int) = when {
        width > height -> maxSide to (height * (maxSide.toFloat() / width)).toInt()
        width < height -> (width * (maxSide.toFloat() / height)).toInt() to maxSide
        else -> maxSide to maxSide
    }
}
