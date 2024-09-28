package io.github.zyrouge.metaphony.id3v2

import io.github.zyrouge.metaphony.utils.xBitSetAt
import io.github.zyrouge.metaphony.utils.xReadByte
import io.github.zyrouge.metaphony.utils.xSkipBytes
import java.io.InputStream

internal data class ID3v2FrameFlags(
    val flagsSize: Int,
    val compression: Boolean,
    val encryption: Boolean,
    val unsynchronization: Boolean,
    val dataLengthIndicator: Boolean,
) {
    companion object {
        internal fun readID3v2FrameFlags(
            input: InputStream,
            version: ID3v2Version,
        ): ID3v2FrameFlags? {
            return when (version) {
                ID3v2Version.V3 -> readID3v2r3FrameFlags(input)
                ID3v2Version.V4 -> readID3v2r4FrameFlags(input)
                else -> null
            }
        }

        private fun readID3v2r3FrameFlags(input: InputStream): ID3v2FrameFlags {
            input.xSkipBytes(1)
            val format = input.xReadByte()
            return ID3v2FrameFlags(
                flagsSize = 2,
                compression = format.xBitSetAt(7),
                encryption = format.xBitSetAt(6),
                unsynchronization = false,
                dataLengthIndicator = false,
            )
        }

        private fun readID3v2r4FrameFlags(input: InputStream): ID3v2FrameFlags {
            input.xSkipBytes(1)
            val format = input.xReadByte()
            return ID3v2FrameFlags(
                flagsSize = 2,
                compression = format.xBitSetAt(3),
                encryption = format.xBitSetAt(2),
                unsynchronization = format.xBitSetAt(1),
                dataLengthIndicator = format.xBitSetAt(0),
            )
        }
    }
}