package io.github.zyrouge.symphony.services.groove.repositories

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.store.findByIdAsFlow
import io.github.zyrouge.symphony.services.database.store.valuesAsFlow
import io.github.zyrouge.symphony.services.database.store.valuesMappedAsFlow

class GenreRepository(private val symphony: Symphony) {
    enum class SortBy {
        CUSTOM,
        GENRE,
        TRACKS_COUNT,
    }

    fun findByIdAsFlow(id: String) = symphony.database.genres.findByIdAsFlow(id)

    fun findSongsByIdAsFlow(id: String, sortBy: SongRepository.SortBy, sortReverse: Boolean) =
        symphony.database.genreSongMapping.valuesMappedAsFlow(
            symphony.database.songs,
            id,
            sortBy,
            sortReverse
        )

    fun valuesAsFlow(sortBy: SortBy, sortReverse: Boolean) =
        symphony.database.genres.valuesAsFlow(sortBy, sortReverse)
}
