package io.github.zyrouge.metaphony.mp3

import io.github.zyrouge.metaphony.id3v2.ID3v2Frames.readID3v2Frames
import io.github.zyrouge.metaphony.id3v2.ID3v2Metadata
import java.io.InputStream

object Mp3 {
    fun read(input: InputStream): ID3v2Metadata {
        val builder = ID3v2Metadata.Builder()
        builder.readID3v2Frames(input)
        return builder.done()
    }
}
