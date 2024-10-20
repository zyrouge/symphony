package io.github.zyrouge.metaphony.audio.ogg

import io.github.zyrouge.metaphony.utils.xDecodeToLEBuffer
import io.github.zyrouge.metaphony.utils.xReadByte
import io.github.zyrouge.metaphony.utils.xReadBytes
import io.github.zyrouge.metaphony.utils.xReadInt
import io.github.zyrouge.metaphony.utils.xReadString
import io.github.zyrouge.metaphony.utils.xSkipBytes
import java.io.InputStream

data class OggPageHeader(
    val segments: Int,
    val isContinuation: Boolean,
    val isFirstPage: Boolean,
    val isLastPage: Boolean,
    val granulePosition: Int,
) {
    companion object {
        fun readOggPageHeader(input: InputStream): OggPageHeader {
            val marker = input.xReadString(4)
            if (marker != "OggS") {
                throw Exception("Missing synchronisation marker")
            }
            val version = input.xReadByte()
            if (version != 0.toByte()) {
                throw Exception("Invalid version")
            }
            val flags = input.xReadInt(1)
            val granulePosition = input.xReadBytes(4).xDecodeToLEBuffer().getInt(0)
//            input.xReadBytes(16).toHexString()
            input.xSkipBytes(16)
            val segments = input.xReadInt(1)
            return OggPageHeader(
                segments = segments,
                isContinuation = flags == 1,
                isFirstPage = flags == 2,
                isLastPage = flags == 4,
                granulePosition = granulePosition,
            )
        }
    }
}
