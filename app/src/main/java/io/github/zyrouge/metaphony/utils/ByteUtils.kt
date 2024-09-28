package io.github.zyrouge.metaphony.utils

internal fun Byte.xDecodeToUInt() = toUByte().toInt()

internal fun Byte.xBitSetAt(n: Int): Boolean {
    return (toInt() shr n) and 1 == 1
}
