package io.github.zyrouge.metaphony.audio.mpeg4

import io.github.zyrouge.metaphony.AudioFile
import io.github.zyrouge.metaphony.metadata.mpeg4.Mpeg4Atoms
import io.github.zyrouge.metaphony.metadata.mpeg4.Mpeg4Metadata
import java.io.InputStream

class Mpeg4 : AudioFile {
    val stream = Mpeg4StreamInfo()
    val metadata = Mpeg4Metadata()

    override fun getMetadata() = metadata.build()
    override fun getStreamInfo() = stream.build()

    override fun read(input: InputStream) {
        Mpeg4Atoms(metadata = metadata, stream = stream).readAtoms(input)
    }
}
