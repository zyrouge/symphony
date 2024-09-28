package io.github.zyrouge.metaphony

import io.github.zyrouge.metaphony.flac.Flac
import io.github.zyrouge.metaphony.mp3.Mp3
import io.github.zyrouge.metaphony.mpeg4.Mpeg4
import io.github.zyrouge.metaphony.ogg.Ogg
import java.io.InputStream
import java.time.LocalDate

interface Metadata {
    val title: String?
    val artists: Set<String>
    val album: String?
    val albumArtists: Set<String>
    val composer: Set<String>
    val genres: Set<String>
    val year: Int?
    val date: LocalDate?
    val trackNumber: Int?
    val trackTotal: Int?
    val discNumber: Int?
    val discTotal: Int?
    val lyrics: String?
    val comments: Set<String>
    val artworks: List<Artwork>

    companion object {
        fun read(input: InputStream, mimeType: String): Metadata? {
            return when (mimeType) {
                "audio/flac" -> Flac.read(input)
                "audio/mpeg" -> Mp3.read(input)
                "audio/mp4" -> Mpeg4.read(input)
                "audio/ogg" -> Ogg.read(input)
                else -> null
            }
        }
    }
}
