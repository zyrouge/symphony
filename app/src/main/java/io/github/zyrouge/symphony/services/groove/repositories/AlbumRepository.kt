package io.github.zyrouge.symphony.services.groove.repositories

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.store.valuesAsFlow
import io.github.zyrouge.symphony.services.database.store.valuesMappedAsFlow

class AlbumRepository(private val symphony: Symphony) {
    enum class SortBy {
        CUSTOM,
        ALBUM_NAME,
        ARTIST_NAME,
        TRACKS_COUNT,
        ARTISTS_COUNT,
        YEAR,
    }

    fun findByIdAsFlow(id: String) = symphony.database.albums.findByIdAsFlow(id)

    fun findSongsByIdAsFlow(id: String, sortBy: SongRepository.SortBy, sortReverse: Boolean) =
        symphony.database.albumSongMapping.valuesMappedAsFlow(
            symphony.database.songs,
            id,
            sortBy,
            sortReverse
        )

    fun valuesAsFlow(sortBy: SortBy, sortReverse: Boolean) =
        symphony.database.albums.valuesAsFlow(sortBy, sortReverse)
}
