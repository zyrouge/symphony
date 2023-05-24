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
    private val cache = ConcurrentHashMap<String, Artist>()
    private val songIdsCache = ConcurrentHashMap<String, ConcurrentSet<Long>>()
    private val albumIdsCache = ConcurrentHashMap<String, ConcurrentSet<Long>>()
    private val searcher = FuzzySearcher<String>(
        options = listOf(
            FuzzySearchOption({ get(it)?.name })
        )
    )

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

    fun search(artistIds: List<String>, terms: String, limit: Int? = 7) = searcher
        .search(terms, artistIds)
        .subListNonStrict(limit ?: artistIds.size)

    fun sort(artistIds: List<String>, by: ArtistSortBy, reversed: Boolean): List<String> {
        val sorted = when (by) {
            ArtistSortBy.CUSTOM -> artistIds.toList()
            ArtistSortBy.ARTIST_NAME -> artistIds.sortedBy { get(it)?.name }
            ArtistSortBy.TRACKS_COUNT -> artistIds.sortedBy { get(it)?.numberOfTracks }
            ArtistSortBy.ALBUMS_COUNT -> artistIds.sortedBy { get(it)?.numberOfTracks }
        }
        return if (reversed) sorted.reversed() else sorted
    }

    fun count() = cache.size
    fun ids() = cache.keys.toList()
    fun values() = cache.values.toList()

    fun get(id: String) = cache[id]
    fun get(ids: List<String>) = ids.mapNotNull { get(it) }
    fun getAlbumIds(artistName: String) = albumIdsCache[artistName]?.toList() ?: listOf()
    fun getSongIds(artistName: String) = songIdsCache[artistName]?.toList() ?: listOf()
}
