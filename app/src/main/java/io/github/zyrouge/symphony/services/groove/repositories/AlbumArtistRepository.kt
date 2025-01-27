package io.github.zyrouge.symphony.services.groove.repositories

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.ui.helpers.createHandyImageRequest
import io.github.zyrouge.symphony.utils.withCase

class AlbumArtistRepository(private val symphony: Symphony) {
    enum class SortBy {
        CUSTOM,
        ARTIST_NAME,
        TRACKS_COUNT,
        ALBUMS_COUNT,
    }

    fun getArtworkUri(albumArtistName: String) = songIdsCache[albumArtistName]?.firstOrNull()
        ?.let { symphony.groove.song.getArtworkUri(it) }
        ?: symphony.groove.song.getDefaultArtworkUri()

    fun createArtworkImageRequest(albumArtistName: String) = createHandyImageRequest(
        symphony.applicationContext,
        image = getArtworkUri(albumArtistName),
        fallback = Assets.placeholderDarkId,
    )

    fun search(albumArtistNames: List<String>, terms: String, limit: Int = 7) = searcher
        .search(terms, albumArtistNames, maxLength = limit)

    fun sort(albumArtistNames: List<String>, by: SortBy, reverse: Boolean): List<String> {
        val sensitive = symphony.settings.caseSensitiveSorting.value
        val sorted = when (by) {
            SortBy.CUSTOM -> albumArtistNames
            SortBy.ARTIST_NAME -> albumArtistNames.sortedBy { get(it)?.name?.withCase(sensitive) }
            SortBy.TRACKS_COUNT -> albumArtistNames.sortedBy { get(it)?.numberOfTracks }
            SortBy.ALBUMS_COUNT -> albumArtistNames.sortedBy { get(it)?.numberOfTracks }
        }
        return if (reverse) sorted.reversed() else sorted
    }

    fun count() = cache.size
    fun ids() = cache.keys.toList()
    fun values() = cache.values.toList()

    fun get(albumArtistName: String) = cache[albumArtistName]
    fun get(albumArtistNames: List<String>) = albumArtistNames.mapNotNull { get(it) }
    fun getAlbumIds(albumArtistName: String) =
        albumIdsCache[albumArtistName]?.toList() ?: emptyList()

    fun getSongIds(albumArtistName: String) = songIdsCache[albumArtistName]?.toList() ?: emptyList()
}
