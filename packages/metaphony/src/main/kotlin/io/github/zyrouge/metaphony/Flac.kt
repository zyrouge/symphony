package io.github.zyrouge.metaphony

import java.io.InputStream
import kotlin.experimental.and

object Flac {
    fun read(input: InputStream) = readFlac(input)
}

private fun readFlac(input: InputStream): VorbisMetadata {
    val flac = input.xReadString(4)
    if (flac != "fLaC") {
        throw Exception("Missing 'fLaC' header")
    }
    val builder = VorbisMetadata.Builder()
    while (true) {
        val last = readFlacBlock(input, builder)
        if (last) break
    }
    return builder.done()
}

private fun readFlacBlock(
    input: InputStream,
    builder: VorbisMetadata.Builder,
): Boolean {
    val blockHeader = input.xReadByte()
    val last = (blockHeader and 0x80.toByte()) == 0x80.toByte()
    val blockId = blockHeader and 0x7f.toByte()
    val blockLen = input.xReadInt(3)
    when (blockId.xDecodeToUInt()) {
        4 -> builder.readVorbisComments(input)
        6 -> builder.readVorbisPicture(input)
        else -> input.xSkipBytes(blockLen)
    }
    return last
}
