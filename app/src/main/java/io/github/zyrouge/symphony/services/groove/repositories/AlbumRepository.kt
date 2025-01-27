package io.github.zyrouge.symphony.services.groove.repositories

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.store.AlbumStore
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.ui.helpers.createHandyImageRequest
import io.github.zyrouge.symphony.utils.withCase

class AlbumRepository(private val symphony: Symphony) {
    enum class SortBy {
        CUSTOM,
        ALBUM_NAME,
        TRACKS_COUNT,
        ARTISTS_COUNT,
        YEAR,
    }

    fun getArtworkUri(albumId: String) = songIdsCache[albumId]?.firstOrNull()
        ?.let { symphony.groove.song.getArtworkUri(it) }
        ?: symphony.groove.song.getDefaultArtworkUri()

    fun createArtworkImageRequest(albumId: String) = createHandyImageRequest(
        symphony.applicationContext,
        image = getArtworkUri(albumId),
        fallback = Assets.placeholderDarkId,
    )

    fun search(albumIds: List<String>, terms: String, limit: Int = 7) = searcher
        .search(terms, albumIds, maxLength = limit)

    fun sort(
        albums: List<AlbumStore.AlbumAlongAttributes>,
        by: SortBy,
        reverse: Boolean,
    ): List<AlbumStore.AlbumAlongAttributes> {
        val sensitive = symphony.settings.caseSensitiveSorting.value
        val sorted = when (by) {
            SortBy.CUSTOM -> albums
            SortBy.ALBUM_NAME -> albums.sortedBy { it.album.name.withCase(sensitive) }
            SortBy.TRACKS_COUNT -> albums.sortedBy { it.tracksCount }
            SortBy.YEAR -> albums.sortedBy { it.album.startYear }
        }
        return if (reverse) sorted.reversed() else sorted
    }
}
