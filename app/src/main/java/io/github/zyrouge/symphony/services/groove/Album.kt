package io.github.zyrouge.symphony.services.groove

import androidx.compose.runtime.Immutable
import io.github.zyrouge.symphony.Symphony

@Immutable
data class Album(
    val id: Long,
    val name: String,
    val artist: String?,
    var numberOfTracks: Int,
) {
    fun createArtworkImageRequest(symphony: Symphony) =
        symphony.groove.album.createArtworkImageRequest(id)
}
