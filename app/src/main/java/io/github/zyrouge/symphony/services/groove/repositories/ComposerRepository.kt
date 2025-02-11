package io.github.zyrouge.symphony.services.groove.repositories

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.store.findTop4SongArtworksAsFlow
import io.github.zyrouge.symphony.services.database.store.valuesAsFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest

class ComposerRepository(private val symphony: Symphony) {
    enum class SortBy {
        CUSTOM,
        COMPOSER_NAME,
        TRACKS_COUNT,
        ALBUMS_COUNT,
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTop4ArtworkUriAsFlow(id: String) =
        symphony.database.composerSongMapping.findTop4SongArtworksAsFlow(id)
            .mapLatest { indices ->
                indices.map { symphony.groove.song.getArtworkUriFromIndex(it) }
            }

    fun valuesAsFlow(sortBy: SortBy, sortReverse: Boolean) =
        symphony.database.composers.valuesAsFlow(sortBy, sortReverse)
}
