package io.github.zyrouge.metaphony

import io.github.zyrouge.metaphony.audio.flac.Flac
import io.github.zyrouge.metaphony.audio.mp3.Mp3
import io.github.zyrouge.metaphony.audio.mpeg4.Mpeg4
import io.github.zyrouge.metaphony.audio.ogg.Ogg
import java.io.InputStream

interface AudioParser {
    fun getMetadata(): AudioMetadata
    fun getStreamInfo(): AudioStreamInfo

    fun read(input: InputStream)

    companion object {
        private fun fromMimeType(mimeType: String) = when (mimeType) {
            "audio/flac" -> Flac()
            "audio/mpeg" -> Mp3()
            "audio/mp4" -> Mpeg4()
            "audio/ogg" -> Ogg()
            else -> null
        }

        fun read(input: InputStream, mimeType: String) = fromMimeType(mimeType)?.also {
            it.read(input)
        }
    }
}
