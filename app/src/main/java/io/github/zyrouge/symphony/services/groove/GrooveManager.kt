package io.github.zyrouge.symphony.services.groove

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.PermissionEvents

enum class GrooveKinds {
    SONG,
    ALBUM,
    ARTIST,
}

class GrooveManager(private val symphony: Symphony) {
    val song = SongRepository(symphony)
    val album = AlbumRepository(symphony)
    val artist = ArtistRepository(symphony)

    init {
        fetch()
        symphony.permission.onUpdate.subscribe {
            when (it) {
                PermissionEvents.MEDIA_PERMISSION_GRANTED -> fetch()
            }
        }
    }

    private fun fetch() {
        song.fetch()
        album.fetch()
        artist.fetch()
    }
}