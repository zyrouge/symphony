package io.github.zyrouge.metaphony.utils

import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.nio.ByteOrder

internal fun InputStream.xSkipBytes(count: Int) {
    if (count < 0) {
        throw Exception("Cannot skip negative count")
    }
    var n = count.toLong()
    // copied from `InputStream.skipNBytes()`
    while (n > 0) {
        val ns = skip(n)
        n -= when (ns) {
            in 1..n -> ns
            0L -> {
                if (read() == -1) {
                    throw EOFException()
                }
                1
            }

            else -> throw IOException("Unable to skip exactly")
        }
    }
}

internal fun InputStream.xReadBytes(n: Int): ByteArray {
    if (n < 0) {
        throw Exception("Cannot read negative count")
    }
    val bytes = ByteArray(n)
    read(bytes, 0, n)
    return bytes
}

internal fun InputStream.xAvailable() = available() > 0
internal fun InputStream.xReadByte(): Byte = xReadBytes(1).first()
internal fun InputStream.xReadString(n: Int) = xReadBytes(n).decodeToString()
internal fun InputStream.xReadInt(n: Int, bitSize: Int = 8) = xReadBytes(n).xDecodeToInt(bitSize)
internal fun InputStream.xReadLEBuffer(n: Int) = xReadOrderedBuffer(n, ByteOrder.LITTLE_ENDIAN)
internal fun InputStream.xReadBEBuffer(n: Int) = xReadOrderedBuffer(n, ByteOrder.BIG_ENDIAN)
internal fun InputStream.xReadOrderedBuffer(n: Int, order: ByteOrder) =
    xReadBytes(n).xDecodeToOrderedBuffer(order)
