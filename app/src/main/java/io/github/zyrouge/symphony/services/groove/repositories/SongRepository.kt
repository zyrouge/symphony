package io.github.zyrouge.symphony.services.groove.repositories

import androidx.core.net.toUri
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.store.valuesAsFlow
import io.github.zyrouge.symphony.services.groove.entities.SongArtworkIndex
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest

class SongRepository(private val symphony: Symphony) {
    enum class SortBy {
        CUSTOM,
        TITLE,
        ARTIST,
        ALBUM,
        DURATION,
        DATE_MODIFIED,
        COMPOSER,
        ALBUM_ARTIST,
        YEAR,
        FILENAME,
        TRACK_NUMBER,
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getArtworkUriAsFlow(id: String) =
        symphony.database.songArtworkIndices.findBySongIdAsFlow(id)
            .mapLatest { index -> index?.let { getArtworkUriFromIndex(it) } }

    fun getArtworkUriFromIndex(index: SongArtworkIndex) = index.file?.let {
        symphony.database.songArtworks.get(it).toUri()
    }

    fun valuesAsFlow(sortBy: SortBy, sortReverse: Boolean) =
        symphony.database.songs.valuesAsFlow(sortBy, sortReverse)
}
