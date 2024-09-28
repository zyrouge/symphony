package io.github.zyrouge.metaphony.ogg

import io.github.zyrouge.metaphony.utils.xAvailable
import io.github.zyrouge.metaphony.utils.xSkipBytes
import io.github.zyrouge.metaphony.utils.xStartsWith
import io.github.zyrouge.metaphony.vorbis.VorbisMetadata
import io.github.zyrouge.metaphony.vorbis.readVorbisComments
import java.io.InputStream

object Ogg {
    private val VORBIS_PREFIX = byteArrayOf(3, 118, 111, 114, 98, 105, 115)

    fun read(input: InputStream): VorbisMetadata {
        val builder = VorbisMetadata.Builder()
        while (input.xAvailable()) {
            val packet = OggPacket.readOggPacket(input)
            if (packet.xStartsWith(VORBIS_PREFIX)) {
                packet.inputStream().use { packetStream ->
                    packetStream.xSkipBytes(VORBIS_PREFIX.size)
                    builder.readVorbisComments(packetStream)
                }
            }
        }
        return builder.done()
    }
}

