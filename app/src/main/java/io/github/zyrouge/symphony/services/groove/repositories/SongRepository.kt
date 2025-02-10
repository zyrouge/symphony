package io.github.zyrouge.symphony.services.groove.repositories

import androidx.core.net.toUri
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.store.valuesAsFlow
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.ui.helpers.createHandyImageRequest

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

    fun createArtworkImageRequest(songId: String) = createHandyImageRequest(
        symphony.applicationContext,
        image = getArtworkUri(songId),
        fallback = Assets.getPlaceholderId(symphony),
    )

    fun getArtworkUri(songId: String) =
        symphony.database.songArtworkIndices.findBySongId(songId)?.file
            ?.let { symphony.database.songArtworks.get(it).toUri() }
            ?: Assets.getPlaceholderUri(symphony)

    fun valuesAsFlow(sortBy: SortBy, sortReverse: Boolean) =
        symphony.database.songs.valuesAsFlow(sortBy, sortReverse)
}
