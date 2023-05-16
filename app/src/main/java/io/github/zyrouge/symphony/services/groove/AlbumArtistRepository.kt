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

enum class AlbumArtistSortBy {
    CUSTOM,
    ARTIST_NAME,
    TRACKS_COUNT,
    ALBUMS_COUNT,
}

class AlbumArtistRepository(private val symphony: Symphony) {
    val cache = ConcurrentHashMap<String, AlbumArtist>()
    val songIdsCache = ConcurrentHashMap<String, ConcurrentSet<Long>>()
    val albumIdsCache = ConcurrentHashMap<String, ConcurrentSet<Long>>()

    val isUpdating get() = symphony.groove.mediaStore.isUpdating
    private val _all = MutableStateFlow<List<String>>(listOf())
    val all = _all.asStateFlow()

    private fun emitAll() = _all.tryEmit(cache.keys.toList())

    internal fun onSong(song: Song) {
        if (song.additional.albumArtist == null) return
        songIdsCache.compute(song.additional.albumArtist) { _, value ->
            value?.apply { add(song.id) }
                ?: ConcurrentSet(song.id)
        }
        var nNumberOfAlbums = 0
        albumIdsCache.compute(song.additional.albumArtist) { _, value ->
            nNumberOfAlbums = (value?.size ?: 0) + 1
            value?.apply { add(song.albumId) }
                ?: ConcurrentSet(song.albumId)
        }
        cache.compute(song.additional.albumArtist) { _, value ->
            value?.apply {
                numberOfAlbums = nNumberOfAlbums
                numberOfTracks++
            } ?: AlbumArtist(
                name = song.additional.albumArtist,
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

    fun getAlbumArtistArtworkUri(artistName: String) =
        albumIdsCache[artistName]?.firstOrNull()?.let {
            symphony.groove.album.getAlbumArtworkUri(it)
        } ?: symphony.groove.album.getDefaultAlbumArtworkUri()

    fun createAlbumArtistArtworkImageRequest(artistName: String) = createHandyImageRequest(
        symphony.applicationContext,
        image = getAlbumArtistArtworkUri(artistName),
        fallback = Assets.placeholderId,
    )

    fun resolveId(id: String) = cache[id]
    fun resolveIds(ids: List<String>) = ids.mapNotNull { id -> resolveId(id) }

    fun count() = cache.size
    fun getAll() = cache.values.toList()
    fun getAlbumArtistFromArtistName(artistName: String) = cache[artistName]

    fun getAlbumIdsOfAlbumArtist(artistName: String) =
        albumIdsCache[artistName]?.toList() ?: listOf()

    fun getAlbumsOfAlbumArtist(artistName: String) = getAlbumIdsOfAlbumArtist(artistName)
        .mapNotNull { symphony.groove.album.getAlbumWithId(it) }

    fun getSongIdsOfAlbumArtist(artistName: String) =
        songIdsCache[artistName]?.toList() ?: listOf()

    fun getSongsOfAlbumArtist(artistName: String) = getSongIdsOfAlbumArtist(artistName)
        .mapNotNull { symphony.groove.song.getSongWithId(it) }

    companion object {
        val searcher = FuzzySearcher<AlbumArtist>(
            options = listOf(
                FuzzySearchOption({ it.name })
            )
        )

        fun search(albumArtists: List<AlbumArtist>, terms: String, limit: Int? = 7) = searcher
            .search(terms, albumArtists)
            .subListNonStrict(limit ?: albumArtists.size)

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
