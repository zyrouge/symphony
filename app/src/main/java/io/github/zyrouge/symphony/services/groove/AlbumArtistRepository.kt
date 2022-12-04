package io.github.zyrouge.symphony.services.groove

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.Eventer
import io.github.zyrouge.symphony.utils.subListNonStrict

class AlbumArtistRepository(private val symphony: Symphony) {
    var isUpdating = false
    val onUpdate = Eventer<Nothing?>()

    fun getAll() = symphony.groove.song.getAlbumArtistNames().mapNotNull { artist ->
        symphony.groove.artist.getArtistFromName(artist)
    }

    fun search(terms: String) =
        symphony.groove.artist.searcher.search(terms, getAll()).subListNonStrict(7)
}