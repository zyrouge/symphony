package io.github.zyrouge.symphony.services.groove.repositories

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.store.valuesAsFlow

class ComposerRepository(private val symphony: Symphony) {
    enum class SortBy {
        CUSTOM,
        COMPOSER_NAME,
        TRACKS_COUNT,
        ALBUMS_COUNT,
    }

    fun valuesAsFlow(sortBy: SortBy, sortReverse: Boolean) =
        symphony.database.composers.valuesAsFlow(sortBy, sortReverse)
}
