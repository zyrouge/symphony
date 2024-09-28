package io.github.zyrouge.metaphony.utils

internal fun ByteArray.xDecodeToInt(bitSize: Int = 8) = fold(0) { value, x ->
    (value shl bitSize) or x.xDecodeToUInt()
}

internal fun ByteArray.xSlice(from: Int = 0, to: Int = size) = copyOfRange(from, to)

internal fun ByteArray.xStartsWith(prefix: ByteArray): Boolean {
    if (size < prefix.size) return false
    for ((i, x) in prefix.withIndex()) {
        if (this[i] != x) return false
    }
    return true
}

internal fun ByteArray.xIndexOf(delimiter: ByteArray, start: Int = 0): Int {
    if (delimiter.isEmpty()) {
        throw NotImplementedError("Expected non-zero sized delimiter")
    }
    for (i in start until size - delimiter.size + 1) {
        val matched = delimiter.withIndex().all {
            it.value == this[i + it.index]
        }
        if (matched) return i
    }
    return -1
}

internal fun ByteArray.xSplit(delimiter: ByteArray, limit: Int = -1): List<ByteArray> {
    if (limit == 0 || limit < -1) {
        throw NotImplementedError("Expected limit to be greater than 0 or -1")
    }
    val values = mutableListOf<ByteArray>()
    var start = 0
    while (values.size < limit) {
        var end = xIndexOf(delimiter, start)
        if (end == -1) {
            values.add(xSlice(start))
            break
        }
        if (values.size + 1 == limit) {
            end = size
        }
        values.add(xSlice(start, end))
        start = end + delimiter.size
    }
    return values
}
