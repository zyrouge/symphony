package io.github.zyrouge.metaphony.ogg

import io.github.zyrouge.metaphony.utils.xReadByte
import io.github.zyrouge.metaphony.utils.xReadInt
import io.github.zyrouge.metaphony.utils.xReadString
import io.github.zyrouge.metaphony.utils.xSkipBytes
import java.io.InputStream

internal data class OggPageHeader(
    val segments: Int,
    val isContinuation: Boolean,
    val isFirstPage: Boolean,
    val isLastPage: Boolean,
) {
    companion object {
        internal fun readOggPageHeader(input: InputStream): OggPageHeader {
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
    }
}
