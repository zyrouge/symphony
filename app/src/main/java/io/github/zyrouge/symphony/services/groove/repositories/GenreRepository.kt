package io.github.zyrouge.symphony.services.groove.repositories

import io.github.zyrouge.symphony.Symphony

class GenreRepository(private val symphony: Symphony) {
    enum class SortBy {
        CUSTOM,
        GENRE,
        TRACKS_COUNT,
    }

    fun findByIdAsFlow(id: String) = symphony.database.genres.findByIdAsFlow(id)
    fun get(id: String) = symphony.database.genres.findById(id)
    fun search(terms: String) = symphony.database.genres.search(terms)

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
