package io.github.zyrouge.metaphony

import io.github.zyrouge.metaphony.audio.flac.Flac
import io.github.zyrouge.metaphony.audio.mp3.Mp3
import io.github.zyrouge.metaphony.audio.mpeg4.Mpeg4
import io.github.zyrouge.metaphony.audio.ogg.Ogg
import java.io.InputStream

interface AudioFile {
    fun getMetadata(): AudioMetadata
    fun getStreamInfo(): AudioStreamInfo
    fun read(input: InputStream)

    companion object {
        fun read(input: InputStream, mimeType: String) = when (mimeType) {
            "audio/flac" -> Flac()
            "audio/mpeg" -> Mp3()
            "audio/mp4" -> Mpeg4()
            "audio/ogg" -> Ogg()
            else -> null
        }?.also { it.read(input) }
    }
}
