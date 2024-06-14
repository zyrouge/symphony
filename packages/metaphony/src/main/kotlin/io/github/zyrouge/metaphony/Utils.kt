package io.github.zyrouge.metaphony

import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal fun InputStream.xSkipBytes(n: Int) {
    if (n < 0) throw Exception("Cannot skip negative count")
    skipNBytes(n.toLong())
}

internal fun InputStream.xReadBytes(n: Int): ByteArray = readNBytes(n)
internal fun InputStream.xReadByte(): Byte = readNBytes(1).first()
internal fun InputStream.xReadString(n: Int) = xReadBytes(n).decodeToString()
internal fun InputStream.xReadInt(n: Int, bitSize: Int = 8) = xReadBytes(n).xDecodeToInt(bitSize)
internal fun InputStream.xRead32bitLittleEndian() = xReadOrderedInt(ByteOrder.LITTLE_ENDIAN)
internal fun InputStream.xRead32bitBigEndian() = xReadOrderedInt(ByteOrder.BIG_ENDIAN)
internal fun InputStream.xAvailable() = available() > 0

internal fun InputStream.xReadOrderedInt(order: ByteOrder): Int {
    val bytes = xReadBytes(4)
    return ByteBuffer.wrap(bytes).order(order).getInt()
}

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

internal fun Byte.xDecodeToUInt() = toUByte().toInt()

internal fun Byte.xBitSetAt(n: Int): Boolean {
    return (toInt() shr n) and 1 == 1
}

internal fun parseDate(value: String): LocalDate {
    val format = when (value.length) {
        4 -> DateTimeFormatter.ofPattern("yyyy")
        7 -> DateTimeFormatter.ofPattern("yyyy-MM")
        10 -> DateTimeFormatter.ofPattern("yyyy-MM-dd")
        else -> DateTimeFormatter.ISO_LOCAL_DATE
    }
    return LocalDate.parse(value, format)
}

internal fun parseSlashSeparatedNumbers(value: String): Pair<Int, Int>? {
    val split = value.split("/")
    if (split.size == 2) {
        return split[0].toInt() to split[1].toInt()
    }
    return null
}

internal fun String.xIntBeforeSlash() = parseSlashSeparatedNumbers(this)?.first
internal fun String.xIntAfterSlash() = parseSlashSeparatedNumbers(this)?.second
