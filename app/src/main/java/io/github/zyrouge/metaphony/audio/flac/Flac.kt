package io.github.zyrouge.metaphony.audio.flac

import io.github.zyrouge.metaphony.AudioParser
import io.github.zyrouge.metaphony.metadata.vorbis.VorbisMetadata
import io.github.zyrouge.metaphony.metadata.vorbis.readVorbisComments
import io.github.zyrouge.metaphony.metadata.vorbis.readVorbisPicture
import io.github.zyrouge.metaphony.utils.xDecodeToUInt
import io.github.zyrouge.metaphony.utils.xReadByte
import io.github.zyrouge.metaphony.utils.xReadInt
import io.github.zyrouge.metaphony.utils.xReadString
import io.github.zyrouge.metaphony.utils.xSkipBytes
import java.io.InputStream
import kotlin.experimental.and

class Flac : AudioParser {
    val stream = FlacStreamInfo()
    val metadata = VorbisMetadata()

    override fun getMetadata() = metadata.build()
    override fun getStreamInfo() = stream.build()

    override fun read(input: InputStream) {
        val flac = input.xReadString(4)
        if (flac != "fLaC") {
            throw Exception("Missing 'fLaC' header")
        }
        while (true) {
            val last = readFlacBlock(input)
            if (last) break
        }
    }

    private fun readFlacBlock(input: InputStream): Boolean {
        val blockHeader = input.xReadByte()
        val last = (blockHeader and 0x80.toByte()) == 0x80.toByte()
        val blockId = blockHeader and 0x7f.toByte()
        val blockLen = input.xReadInt(3)
        when (blockId.xDecodeToUInt()) {
            0 -> stream.readVorbisStreamInfo(input)
            4 -> metadata.readVorbisComments(input)
            6 -> metadata.readVorbisPicture(input)
            else -> input.xSkipBytes(blockLen)
        }
        return last
    }
}
