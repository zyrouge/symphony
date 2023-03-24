package io.github.zyrouge.symphony.services.database

import io.github.zyrouge.symphony.Symphony

class Database(symphony: Symphony) {
    val songCache = SongCache(symphony)
    val lyricsCache = LyricsCache(symphony)
    val playlists = PlaylistsBox(symphony)
}
