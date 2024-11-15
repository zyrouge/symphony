package io.github.zyrouge.symphony.services.groove

import androidx.compose.runtime.Immutable
import io.github.zyrouge.symphony.Symphony
import kotlin.time.Duration

@Immutable
data class Album(
    val id: String,
    val name: String,
    val artists: MutableSet<String>,
    var numberOfTracks: Int,
    var duration: Duration,
) {
    fun createArtworkImageRequest(symphony: Symphony) =
        symphony.groove.album.createArtworkImageRequest(id)

    fun getSongIds(symphony: Symphony) = symphony.groove.album.getSongIds(id)
    fun getSortedSongIds(symphony: Symphony) = symphony.groove.song.sort(
        getSongIds(symphony),
        symphony.settings.lastUsedAlbumSongsSortBy.value,
        symphony.settings.lastUsedAlbumSongsSortReverse.value,
    )
}
