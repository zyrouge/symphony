package io.github.zyrouge.symphony.services.groove

import androidx.compose.runtime.Immutable
import io.github.zyrouge.symphony.Symphony

@Immutable
data class AlbumArtist(
    val name: String,
    val albumIdsSet: MutableSet<Long>,
    var numberOfTracks: Int,
) {
    val numberOfAlbums: Int get() = albumIdsSet.size
    val albumIds: List<Long> get() = albumIdsSet.toList()

    fun createArtworkImageRequest(symphony: Symphony) =
        symphony.groove.albumArtist.createAlbumArtistArtworkImageRequest(name)
}
