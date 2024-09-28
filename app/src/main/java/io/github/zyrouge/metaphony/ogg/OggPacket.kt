package io.github.zyrouge.metaphony.ogg

import io.github.zyrouge.metaphony.utils.xReadBytes
import io.github.zyrouge.metaphony.utils.xReadInt
import java.io.InputStream

object OggPacket {
    internal fun readOggPacket(input: InputStream): ByteArray {
        var packet = ByteArray(0)
        var isContinuing = false
        while (true) {
            val header = OggPageHeader.readOggPageHeader(input)
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
}