package io.github.zyrouge.metaphony

import java.time.LocalDate

interface Metadata {
    val title: String?
    val artists: Set<String>
    val album: String?
    val albumArtists: Set<String>
    val composer: String?
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
}
