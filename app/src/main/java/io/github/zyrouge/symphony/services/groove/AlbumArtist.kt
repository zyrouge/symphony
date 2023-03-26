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
        symphony.groove.albumArtist.createAlbumArtistArtworkImageRequest(name)
}
