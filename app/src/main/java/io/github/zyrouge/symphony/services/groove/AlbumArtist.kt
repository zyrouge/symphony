package io.github.zyrouge.symphony.services.groove

import androidx.compose.runtime.Immutable
import io.github.zyrouge.symphony.Symphony

@Immutable
data class AlbumArtist(
    val name: String,
    var numberOfAlbums: Int,
    var numberOfTracks: Int,
) {
    fun createArtworkImageRequest(symphony: Symphony) =
        symphony.groove.albumArtist.createArtworkImageRequest(name)

    fun getSongIds(symphony: Symphony) = symphony.groove.albumArtist.getSongIds(name)
    fun getSortedSongIds(symphony: Symphony) = symphony.groove.song.sort(
        getSongIds(symphony),
        symphony.settings.getLastUsedSongsSortBy(),
        symphony.settings.getLastUsedSongsSortReverse(),
    )

    fun getAlbumIds(symphony: Symphony) = symphony.groove.albumArtist.getAlbumIds(name)
}
