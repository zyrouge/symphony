package io.github.zyrouge.symphony.services.groove

import androidx.compose.runtime.Immutable
import io.github.zyrouge.symphony.Symphony

@Immutable
data class Album(
    val name: String,
    val artists: MutableSet<String>,
    var numberOfTracks: Int,
) {
    fun createArtworkImageRequest(symphony: Symphony) =
        symphony.groove.album.createArtworkImageRequest(name)

    fun getSongIds(symphony: Symphony) = symphony.groove.album.getSongIds(name)
    fun getSortedSongIds(symphony: Symphony) = symphony.groove.song.sort(
        getSongIds(symphony),
        symphony.settings.getLastUsedAlbumSongsSortBy(),
        symphony.settings.getLastUsedAlbumSongsSortReverse(),
    )
}
