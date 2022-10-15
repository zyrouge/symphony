package io.github.zyrouge.symphony.services.groove

import io.github.zyrouge.symphony.Symphony

class GrooveManager(private val symphony: Symphony) {
    val song = SongRepository(symphony)
    val album = AlbumRepository(symphony)
    val artist = ArtistRepository(symphony)
}