package io.github.zyrouge.metaphony.utils

import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal fun InputStream.xSkipBytes(n: Int) {
    if (n < 0) {
        throw Exception("Cannot skip negative count")
    }
    skip(n.toLong())
}

internal fun InputStream.xReadBytes(n: Int): ByteArray {
    if (n < 0) {
        throw Exception("Cannot read negative count")
    }
    val bytes = ByteArray(n)
    read(bytes, 0, n)
    return bytes
}

internal fun InputStream.xReadByte(): Byte = xReadBytes(1).first()
internal fun InputStream.xReadString(n: Int) = xReadBytes(n).decodeToString()
internal fun InputStream.xReadInt(n: Int, bitSize: Int = 8) = xReadBytes(n).xDecodeToInt(bitSize)
internal fun InputStream.xRead32bitLittleEndian() = xReadOrderedInt(ByteOrder.LITTLE_ENDIAN)
internal fun InputStream.xRead32bitBigEndian() = xReadOrderedInt(ByteOrder.BIG_ENDIAN)
internal fun InputStream.xAvailable() = available() > 0

internal fun InputStream.xReadOrderedInt(order: ByteOrder): Int {
    val bytes = xReadBytes(4)
    return ByteBuffer.wrap(bytes).order(order).getInt()
}
