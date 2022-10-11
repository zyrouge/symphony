package io.github.zyrouge.symphony.services.groove

class GrooveManager {
    lateinit var song: SongRepository
    lateinit var album: AlbumRepository
    lateinit var artist: ArtistRepository

    fun init() {
        song = SongRepository()
        song.init()
        album = AlbumRepository()
        album.init()
        artist = ArtistRepository()
        artist.init()
    }
}