package io.github.zyrouge.metaphony.audio.ogg

import io.github.zyrouge.metaphony.AudioParser
import io.github.zyrouge.metaphony.metadata.vorbis.VorbisMetadata
import io.github.zyrouge.metaphony.metadata.vorbis.readVorbisComments
import io.github.zyrouge.metaphony.utils.xAvailable
import io.github.zyrouge.metaphony.utils.xReadBytes
import io.github.zyrouge.metaphony.utils.xReadInt
import io.github.zyrouge.metaphony.utils.xSkipBytes
import io.github.zyrouge.metaphony.utils.xStartsWith
import java.io.InputStream

class Ogg : AudioParser {
    val metadata = VorbisMetadata()
    val stream = OggStreamInfo()

    override fun getMetadata() = metadata.build()
    override fun getStreamInfo() = stream.build()

    override fun read(input: InputStream) {
        while (input.xAvailable()) {
            readOggPacket(input)
        }
    }

    private fun readOggPacket(input: InputStream) {
        var packet = ByteArray(0)
        var isContinuing = false
        while (true) {
            val header = OggPageHeader.readOggPageHeader(input)
            if (isContinuing && !header.isContinuation) {
                throw Exception("Expected continuation page")
            }
            var partialLen = 0
            for (i in 0 until header.segments) {
                val segmentLen = input.xReadInt(1)
                partialLen += segmentLen
            }
            val partial = input.xReadBytes(partialLen)
            packet += partial
            if (header.isLastPage) {
                stream.readOggPageHeader(header)
                break
            }
            if (partialLen % 255 != 0) {
                break
            }
            isContinuing = true
        }
        when {
            packet.xStartsWith(ONE_VORBIS_PREFIX) -> {
                stream.readVorbisComments(packet)
            }

            packet.xStartsWith(THREE_VORBIS_PREFIX) -> {
                packet.inputStream().use { packetStream ->
                    packetStream.xSkipBytes(THREE_VORBIS_PREFIX.size)
                    metadata.readVorbisComments(packetStream)
                }
            }
        }
    }

    companion object {
        private val ONE_VORBIS_PREFIX = byteArrayOf(1, 118, 111, 114, 98, 105, 115)
        private val THREE_VORBIS_PREFIX = byteArrayOf(3, 118, 111, 114, 98, 105, 115)
    }
}
