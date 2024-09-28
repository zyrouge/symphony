package io.github.zyrouge.metaphony.id3v2

import io.github.zyrouge.metaphony.utils.xBitSetAt
import io.github.zyrouge.metaphony.utils.xReadByte
import io.github.zyrouge.metaphony.utils.xReadInt
import io.github.zyrouge.metaphony.utils.xReadString
import io.github.zyrouge.metaphony.utils.xSkipBytes
import java.io.InputStream

internal data class Id3v2Header(
    val version: ID3v2Version,
    val size: Int,
    val unsynchronization: Boolean,
    val offset: Int,
) {
    companion object {
        internal fun readID3v2Header(input: InputStream): Id3v2Header {
            val marker = input.xReadString(3)
            if (marker != "ID3") {
                throw Exception("Missing marker")
            }
            val rawVersion = input.xReadInt(1)
            val version = when (rawVersion) {
                2 -> ID3v2Version.V2
                3 -> ID3v2Version.V3
                4 -> ID3v2Version.V4
                else -> throw Exception("Invalid version")
            }
            input.xSkipBytes(1)
            val flags = input.xReadByte()
            val unsynchronization = flags.xBitSetAt(7)
            val extendedHeader = flags.xBitSetAt(6)
            val size = input.xReadInt(4, 7)
            var offset = 10
            if (extendedHeader) {
                val extendedHeaderSize = when (version) {
                    ID3v2Version.V2 -> 0
                    ID3v2Version.V3 -> input.xReadInt(4)
                    ID3v2Version.V4 -> input.xReadInt(4, 7) - 4
                }
                input.xSkipBytes(extendedHeaderSize)
                offset += extendedHeaderSize
            }
            return Id3v2Header(
                version = version,
                size = size,
                unsynchronization = unsynchronization,
                offset = offset,
            )
        }
    }
}
