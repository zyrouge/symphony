package io.github.zyrouge.symphony.services.i18n

sealed class Translations(
    val language: String,
    val songs: String,
    val artists: String,
    val albums: String,
    val options: String,
    val settings: String,
    val details: String,
    val path: String,
    val filename: String,
    val size: String,
    val dateAdded: String,
    val lastModified: String,
    val length: String,
    val bitrate: String,
    val trackName: String,
    val artist: String,
    val album: String,
    val albumArtist: String,
    val composer: String,
    val nothingIsBeingPlayedRightNow: String,
    val playedXofY: (x: Int, y: Int) -> String,
    val unk: String = "?"
) {
    object english : Translations(
        language = "English",
        songs = "Songs",
        artists = "Artists",
        albums = "Albums",
        options = "Options",
        settings = "Settings",
        details = "Details",
        path = "Path",
        filename = "Filename",
        size = "Size",
        dateAdded = "Date Added",
        lastModified = "Last Modified",
        length = "Length",
        bitrate = "Bitrate",
        trackName = "Track Name",
        artist = "Artist",
        album = "Album",
        albumArtist = "Album Artist",
        composer = "Composer",
        nothingIsBeingPlayedRightNow = "Nothing is being played right now",
        playedXofY = { x, y -> "Played $x of $y" }
    )

    companion object {
        val all = arrayOf(english)
        val default = english
    }
}