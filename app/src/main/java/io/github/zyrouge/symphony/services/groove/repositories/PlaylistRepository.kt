package io.github.zyrouge.symphony.services.groove.repositories

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.store.valuesAsFlow

class PlaylistRepository(private val symphony: Symphony) {
    enum class SortBy {
        CUSTOM,
        TITLE,
        TRACKS_COUNT,
    }

    fun valuesAsFlow(sortBy: SortBy, sortReverse: Boolean) =
        symphony.database.playlists.valuesAsFlow(sortBy, sortReverse)

    companion object {
        private const val FAVORITE_PLAYLIST = "favorites"
    }
}
