package io.github.zyrouge.symphony.utils

import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File

object AudioTaggerX {
    fun <T> read(file: File, use: (AudioFile) -> T) = use(AudioFileIO.read(file))

    fun getLyrics(file: File) = read(file) { audioFile ->
        audioFile.tagOrCreateDefault.getFirst(FieldKey.LYRICS)
            .trim()
            .takeIf { it.isNotEmpty() }
    }
}
