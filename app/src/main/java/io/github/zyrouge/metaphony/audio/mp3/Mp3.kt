package io.github.zyrouge.metaphony.audio.mp3

import io.github.zyrouge.metaphony.AudioFile
import io.github.zyrouge.metaphony.metadata.id3v2.ID3v2Frames.readID3v2Frames
import io.github.zyrouge.metaphony.metadata.id3v2.ID3v2Metadata
import java.io.InputStream

class Mp3 : AudioFile {
    val metadata = ID3v2Metadata()
    val stream = Mp3StreamInfo()

    override fun getMetadata() = metadata.build()
    override fun getStreamInfo() = stream.build()

    override fun read(input: InputStream) {
        metadata.readID3v2Frames(input)
        stream.readStreamInfo(input)
    }
}
