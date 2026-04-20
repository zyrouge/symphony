package me.zyrouge.symphony.metaphony

import java.time.LocalDate
import java.time.Year

data class AudioMetadata(
    val title: String?,
    val album: String?,
    val artists: Set<String>,
    val albumArtists: Set<String>,
    val composers: Set<String>,
    val genres: Set<String>,
    val discNumber: Int?,
    val discTotal: Int?,
    val trackNumber: Int?,
    val trackTotal: Int?,
    val date: LocalDate?,
    val year: Int?,
    val lyrics: String?,
    val encoding: String?,
    val bitrate: Int?,
    val lengthInSeconds: Int?,
    val sampleRate: Int?,
    val channels: Int?,
    val pictures: List<Picture>,
) {
    data class Picture(val pictureType: String, val mimeType: String, val data: ByteArray)
}
