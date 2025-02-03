package io.github.zyrouge.symphony.services.groove.repositories

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.store.valuesAsFlow

class ArtistRepository(private val symphony: Symphony) {
    enum class SortBy {
        CUSTOM,
        ARTIST_NAME,
        TRACKS_COUNT,
        ALBUMS_COUNT,
    }

    fun valuesAsFlow(sortBy: SortBy, sortReverse: Boolean) =
        symphony.database.artists.valuesAsFlow(sortBy, sortReverse)
}
