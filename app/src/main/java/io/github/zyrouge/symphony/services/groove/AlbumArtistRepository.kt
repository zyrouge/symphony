package io.github.zyrouge.symphony.services.groove

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.ui.helpers.createHandyImageRequest
import io.github.zyrouge.symphony.utils.ConcurrentSet
import io.github.zyrouge.symphony.utils.FuzzySearchOption
import io.github.zyrouge.symphony.utils.FuzzySearcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.ConcurrentHashMap

enum class AlbumArtistSortBy {
    CUSTOM,
    ARTIST_NAME,
    TRACKS_COUNT,
    ALBUMS_COUNT,
}

class AlbumArtistRepository(private val symphony: Symphony) {
    private val cache = ConcurrentHashMap<String, AlbumArtist>()
    private val songIdsCache = ConcurrentHashMap<String, ConcurrentSet<Long>>()
    private val albumIdsCache = ConcurrentHashMap<String, ConcurrentSet<Long>>()
    private val searcher = FuzzySearcher<String>(
        options = listOf(FuzzySearchOption({ get(it)?.name }))
    )

    val isUpdating get() = symphony.groove.mediaStore.isUpdating
    private val _all = MutableStateFlow<List<String>>(emptyList())
    val all = _all.asStateFlow()
    private val _count = MutableStateFlow(0)
    val count = _count.asStateFlow()

    private fun emitCount() = _count.update {
        cache.size
    }

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
            } ?: run {
                _all.update {
                    it + song.additional.albumArtist
                }
                emitCount()
                AlbumArtist(
                    name = song.additional.albumArtist,
                    numberOfAlbums = 1,
                    numberOfTracks = 1,
                )
            }
        }
    }

    internal fun onFinish() {}

    fun reset() {
        cache.clear()
        _all.update {
            emptyList()
        }
        emitCount()
    }

    fun getArtworkUri(artistName: String) =
        albumIdsCache[artistName]?.firstOrNull()?.let {
            symphony.groove.album.getArtworkUri(it)
        } ?: symphony.groove.album.getDefaultArtworkUri()

    fun createArtworkImageRequest(artistName: String) = createHandyImageRequest(
        symphony.applicationContext,
        image = getArtworkUri(artistName),
        fallback = Assets.placeholderId,
    )

    fun search(albumArtistIds: List<String>, terms: String, limit: Int = 7) = searcher
        .search(terms, albumArtistIds, maxLength = limit)

    fun sort(
        albumArtistIds: List<String>,
        by: AlbumArtistSortBy,
        reverse: Boolean,
    ): List<String> {
        val sorted = when (by) {
            AlbumArtistSortBy.CUSTOM -> albumArtistIds
            AlbumArtistSortBy.ARTIST_NAME -> albumArtistIds.sortedBy { get(it)?.name }
            AlbumArtistSortBy.TRACKS_COUNT -> albumArtistIds.sortedBy { get(it)?.numberOfTracks }
            AlbumArtistSortBy.ALBUMS_COUNT -> albumArtistIds.sortedBy { get(it)?.numberOfTracks }
        }
        return if (reverse) sorted.reversed() else sorted
    }

    fun count() = cache.size
    fun ids() = cache.keys.toList()
    fun values() = cache.values.toList()

    fun get(artistName: String) = cache[artistName]
    fun get(artistNames: List<String>) = artistNames.mapNotNull { get(it) }
    fun getAlbumIds(artistName: String) = albumIdsCache[artistName]?.toList() ?: emptyList()
    fun getSongIds(artistName: String) = songIdsCache[artistName]?.toList() ?: emptyList()
}
