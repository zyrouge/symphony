package io.github.zyrouge.symphony.utils

import kotlin.math.max

enum class ImageQuality(val maxSide: Int?) {
    Low(256),
    Medium(512),
    High(1024),
    Loseless(null),
}

fun calculateResizedDimensions(width: Int, height: Int, maxSide: Int?): Pair<Int, Int>? {
    if (maxSide == null || max(width, height) < maxSide) {
        return null
    }
    return when {
        width > height -> maxSide to (height * (maxSide.toFloat() / width)).toInt()
        width < height -> (width * (maxSide.toFloat() / height)).toInt() to maxSide
        else -> maxSide to maxSide
    }
}
