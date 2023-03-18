package io.github.zyrouge.symphony.services.groove

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.ui.helpers.createHandyImageRequest
import io.github.zyrouge.symphony.utils.Eventer
import io.github.zyrouge.symphony.utils.FuzzySearchOption
import io.github.zyrouge.symphony.utils.FuzzySearcher
import io.github.zyrouge.symphony.utils.subListNonStrict
import java.util.concurrent.ConcurrentHashMap

enum class AlbumArtistSortBy {
    CUSTOM,
    ARTIST_NAME,
    TRACKS_COUNT,
    ALBUMS_COUNT,
}

class AlbumArtistRepository(private val symphony: Symphony) {
    internal val cached = ConcurrentHashMap<String, AlbumArtist>()
    var isUpdating = false
    val onUpdate = Eventer<Nothing?>()

    private val searcher = FuzzySearcher<AlbumArtist>(
        options = listOf(
            FuzzySearchOption({ it.name })
        )
    )

    fun reset() {
        cached.clear()
        onUpdate.dispatch(null)
    }

    fun getAlbumArtistArtworkUri(artistName: String) =
        cached[artistName]?.albumIdsSet?.firstOrNull()?.let {
            symphony.groove.album.getAlbumArtworkUri(it)
        } ?: symphony.groove.album.getDefaultAlbumArtworkUri()

    fun createAlbumArtistArtworkImageRequest(artistName: String) = createHandyImageRequest(
        symphony.applicationContext,
        image = getAlbumArtistArtworkUri(artistName),
        fallback = Assets.placeholderId,
    )

    fun getAll() = cached.values.toList()
    fun getAlbumArtistFromName(artistName: String) = cached[artistName]

    fun search(terms: String) = searcher.search(terms, getAll()).subListNonStrict(7)

    companion object {
        fun sort(
            albumArtists: List<AlbumArtist>,
            by: AlbumArtistSortBy,
            reversed: Boolean
        ): List<AlbumArtist> {
            val sorted = when (by) {
                AlbumArtistSortBy.CUSTOM -> albumArtists.toList()
                AlbumArtistSortBy.ARTIST_NAME -> albumArtists.sortedBy { it.name }
                AlbumArtistSortBy.TRACKS_COUNT -> albumArtists.sortedBy { it.numberOfTracks }
                AlbumArtistSortBy.ALBUMS_COUNT -> albumArtists.sortedBy { it.numberOfTracks }
            }
            return if (reversed) sorted.reversed() else sorted
        }
    }
}
