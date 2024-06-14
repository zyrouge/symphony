package io.github.zyrouge.metaphony

import java.io.InputStream

object Ogg {
    fun read(input: InputStream) = readOgg(input)
}

private val vorbisPrefix = byteArrayOf(3, 118, 111, 114, 98, 105, 115)

private fun readOgg(input: InputStream): VorbisMetadata {
    val builder = VorbisMetadata.Builder()
    while (input.xAvailable()) {
        val packet = readOggPacket(input)
        if (packet.xStartsWith(vorbisPrefix)) {
            packet.inputStream().use { packetStream ->
                packetStream.xSkipBytes(vorbisPrefix.size)
                builder.readVorbisComments(packetStream)
            }
        }
    }
    return builder.done()
}

private data class OggPageHeader(
    val segments: Int,
    val isContinuation: Boolean,
    val isFirstPage: Boolean,
    val isLastPage: Boolean,
)

private fun readOggPageHeader(input: InputStream): OggPageHeader {
    val marker = input.xReadString(4)
    if (marker != "OggS") {
        throw Exception("Missing synchronisation marker")
    }
    val version = input.xReadByte()
    if (version != 0.toByte()) {
        throw Exception("Invalid version")
    }
    val flags = input.xReadInt(1)
    input.xSkipBytes(20)
    val segments = input.xReadInt(1)
    return OggPageHeader(
        segments = segments,
        isContinuation = flags == 1,
        isFirstPage = flags == 2,
        isLastPage = flags == 4,
    )
}

private fun readOggPacket(input: InputStream): ByteArray {
    var packet = ByteArray(0)
    var isContinuing = false
    while (true) {
        val header = readOggPageHeader(input)
        if (isContinuing && !header.isContinuation) {
            throw Exception("Expected continuation page")
        }
        var partialLen = 0
        var lastSegmentLen = 0
        for (i in 0 until header.segments) {
            val segmentLen = input.xReadInt(1)
            partialLen += segmentLen
            lastSegmentLen = segmentLen
        }
        val partial = input.xReadBytes(partialLen)
        packet += partial
        if (header.isLastPage || lastSegmentLen != 255) {
            break
        }
        isContinuing = true
    }
    return packet
}
