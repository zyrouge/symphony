package io.github.zyrouge.symphony.utils

import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.TagOptionSingleton
import java.io.File

object AudioTaggerX {
    fun <T> read(file: File, use: (AudioFile) -> T): T {
        TagOptionSingleton.getInstance().isAndroid = true
        val audioFile = AudioFileIO.read(file)
        return use(audioFile)
    }

    fun getLyrics(file: File) = read(file) { audioFile ->
        audioFile.tagOrCreateDefault.getFirst(FieldKey.LYRICS)
            .takeIf { it.isNotBlank() }
    }
}
