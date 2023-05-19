package io.github.zyrouge.symphony.services.groove

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.ui.helpers.createHandyImageRequest
import io.github.zyrouge.symphony.utils.ConcurrentSet
import io.github.zyrouge.symphony.utils.FuzzySearchOption
import io.github.zyrouge.symphony.utils.FuzzySearcher
import io.github.zyrouge.symphony.utils.subListNonStrict
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

enum class ArtistSortBy {
    CUSTOM,
    ARTIST_NAME,
    TRACKS_COUNT,
    ALBUMS_COUNT,
}

class ArtistRepository(private val symphony: Symphony) {
    val cache = ConcurrentHashMap<String, Artist>()
    val songIdsCache = ConcurrentHashMap<String, ConcurrentSet<Long>>()
    val albumIdsCache = ConcurrentHashMap<String, ConcurrentSet<Long>>()

    val isUpdating get() = symphony.groove.mediaStore.isUpdating
    private val _all = MutableStateFlow<List<String>>(listOf())
    val all = _all.asStateFlow()

    private fun emitAll() = _all.tryEmit(ids())

    internal fun onSong(song: Song) {
        if (song.artistName == null) return
        songIdsCache.compute(song.artistName) { _, value ->
            value?.apply { add(song.id) }
                ?: ConcurrentSet(song.id)
        }
        var nNumberOfAlbums = 0
        albumIdsCache.compute(song.artistName) { _, value ->
            nNumberOfAlbums = (value?.size ?: 0) + 1
            value?.apply { add(song.albumId) }
                ?: ConcurrentSet(song.albumId)
        }
        cache.compute(song.artistName) { _, value ->
            value?.apply {
                numberOfAlbums = nNumberOfAlbums
                numberOfTracks++
            } ?: Artist(
                name = song.artistName,
                numberOfAlbums = 1,
                numberOfTracks = 1,
            )
        }
        emitAll()
    }

    fun reset() {
        cache.clear()
        emitAll()
    }

    fun getArtworkUri(artistName: String) = albumIdsCache[artistName]?.firstOrNull()
        ?.let { symphony.groove.album.getArtworkUri(it) }
        ?: symphony.groove.album.getDefaultArtworkUri()

    fun createArtworkImageRequest(artistName: String) = createHandyImageRequest(
        symphony.applicationContext,
        image = getArtworkUri(artistName),
        fallback = Assets.placeholderId,
    )

    fun count() = cache.size
    fun ids() = cache.keys.toList()
    fun values() = cache.values.toList()

    fun get(artistName: String) = cache[artistName]
    fun getAlbumIds(artistName: String) = albumIdsCache[artistName] ?: listOf()
    fun getSongIds(artistName: String) = songIdsCache[artistName]?.toList() ?: listOf()

    companion object {
        val searcher = FuzzySearcher<Artist>(
            options = listOf(
                FuzzySearchOption({ it.name })
            )
        )

        fun search(artists: List<Artist>, terms: String, limit: Int? = 7) = searcher
            .search(terms, artists)
            .subListNonStrict(limit ?: artists.size)

        fun sort(artists: List<Artist>, by: ArtistSortBy, reversed: Boolean): List<Artist> {
            val sorted = when (by) {
                ArtistSortBy.CUSTOM -> artists.toList()
                ArtistSortBy.ARTIST_NAME -> artists.sortedBy { it.name }
                ArtistSortBy.TRACKS_COUNT -> artists.sortedBy { it.numberOfTracks }
                ArtistSortBy.ALBUMS_COUNT -> artists.sortedBy { it.numberOfTracks }
            }
            return if (reversed) sorted.reversed() else sorted
        }
    }
}
