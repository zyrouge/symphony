package io.github.zyrouge.symphony.services.groove

import androidx.compose.runtime.Immutable
import io.github.zyrouge.symphony.Symphony

@Immutable
data class Genre(
    val name: String,
    var numberOfTracks: Int,
) {
    fun getSongIds(symphony: Symphony) = symphony.groove.genre.getSongIds(name)
    fun getSortedSongIds(symphony: Symphony) = symphony.groove.song.sort(
        getSongIds(symphony),
        symphony.settings.getLastUsedSongsSortBy(),
        symphony.settings.getLastUsedSongsSortReverse(),
    )
}
