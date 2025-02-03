package io.github.zyrouge.symphony.services.groove.repositories

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.store.valuesAsFlow

class GenreRepository(private val symphony: Symphony) {
    enum class SortBy {
        CUSTOM,
        GENRE,
        TRACKS_COUNT,
    }

    fun valuesAsFlow(sortBy: SortBy, sortReverse: Boolean) =
        symphony.database.genres.valuesAsFlow(sortBy, sortReverse)
}
