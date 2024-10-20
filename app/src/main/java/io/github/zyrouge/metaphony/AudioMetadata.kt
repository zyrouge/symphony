package io.github.zyrouge.metaphony

import androidx.compose.runtime.Immutable
import java.time.LocalDate

@Immutable
data class AudioMetadata(
    val title: String?,
    val artists: Set<String>,
    val album: String?,
    val albumArtists: Set<String>,
    val composer: Set<String>,
    val genres: Set<String>,
    val year: Int?,
    val date: LocalDate?,
    val trackNumber: Int?,
    val trackTotal: Int?,
    val discNumber: Int?,
    val discTotal: Int?,
    val encoder: String?,
    val lyrics: String?,
    val comments: Set<String>,
    val artworks: List<AudioArtwork>,
) {
    interface Buildable {
        fun title(): String?
        fun artists(): Set<String>
        fun album(): String?
        fun albumArtists(): Set<String>
        fun composer(): Set<String>
        fun genres(): Set<String>
        fun year(): Int?
        fun date(): LocalDate?
        fun trackNumber(): Int?
        fun trackTotal(): Int?
        fun discNumber(): Int?
        fun discTotal(): Int?
        fun lyrics(): String?
        fun comments(): Set<String>
        fun encoder(): String?
        fun artworks(): List<AudioArtwork>

        fun build() = AudioMetadata(
            title = title(),
            artists = artists(),
            album = album(),
            albumArtists = albumArtists(),
            composer = composer(),
            genres = genres(),
            year = year(),
            trackNumber = trackNumber(),
            trackTotal = trackTotal(),
            discNumber = discNumber(),
            discTotal = discTotal(),
            encoder = encoder(),
            lyrics = lyrics(),
            comments = comments(),
            date = date(),
            artworks = artworks(),
        )
    }
}
