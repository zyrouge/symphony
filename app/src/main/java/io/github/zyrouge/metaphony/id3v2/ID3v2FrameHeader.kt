package io.github.zyrouge.metaphony.id3v2

import io.github.zyrouge.metaphony.utils.xReadInt
import io.github.zyrouge.metaphony.utils.xReadString
import java.io.InputStream

internal data class ID3v2FrameHeader(
    val name: String,
    val size: Int,
    val headerSize: Int,
) {
    companion object {
        internal fun readID3v2FrameHeader(
            input: InputStream,
            version: ID3v2Version,
        ): ID3v2FrameHeader {
            return when (version) {
                ID3v2Version.V2 -> readID3v2r2FrameHeader(input)
                ID3v2Version.V3 -> readID3v2r3FrameHeader(input)
                ID3v2Version.V4 -> readID3v2r4FrameHeader(input)
            }
        }

        private fun readID3v2r2FrameHeader(input: InputStream): ID3v2FrameHeader {
            val name = input.xReadString(3)
            val size = input.xReadInt(3)
            return ID3v2FrameHeader(
                name = name,
                size = size,
                headerSize = 6,
            )
        }

        private fun readID3v2r3FrameHeader(input: InputStream): ID3v2FrameHeader {
            val name = input.xReadString(4)
            val size = input.xReadInt(4)
            return ID3v2FrameHeader(
                name = name,
                size = size,
                headerSize = 8,
            )
        }

        private fun readID3v2r4FrameHeader(input: InputStream): ID3v2FrameHeader {
            val name = input.xReadString(4)
            val size = input.xReadInt(4, 7)
            return ID3v2FrameHeader(
                name = name,
                size = size,
                headerSize = 8,
            )
        }
    }
}