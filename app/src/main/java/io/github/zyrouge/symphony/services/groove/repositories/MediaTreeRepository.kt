package io.github.zyrouge.symphony.services.groove.repositories

import io.github.zyrouge.symphony.Symphony
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest

class MediaTreeRepository(private val symphony: Symphony) {
    enum class SortBy {
        CUSTOM,
        TITLE,
        TRACKS_COUNT,
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTop4ArtworkUriAsFlow(id: String) =
        symphony.database.playlistSongMapping.findTop4SongArtworksAsFlow(id)
            .mapLatest { indices ->
                indices.map { symphony.groove.song.getArtworkUriFromIndex(it) }
            }

    fun valuesAsFlow(sortBy: SortBy, sortReverse: Boolean) =
        symphony.database.mediaTreeFolders.valuesAsFlow(sortBy, sortReverse)

    companion object {
        private const val FAVORITE_PLAYLIST = "favorites"
    }
}
