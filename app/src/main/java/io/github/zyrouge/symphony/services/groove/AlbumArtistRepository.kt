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
    private val albumIdsCache = ConcurrentHashMap<String, ConcurrentSet<String>>()
    private val searcher = FuzzySearcher<String>(
        options = listOf(FuzzySearchOption({ v -> get(v)?.name?.let { compareString(it) } }))
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
        song.additional.albumArtists.forEach { albumArtist ->
            songIdsCache.compute(albumArtist) { _, value ->
                value?.apply { add(song.id) }
                    ?: ConcurrentSet(song.id)
            }
            var nNumberOfAlbums = 0
            symphony.groove.album.getIdFromSong(song)?.let { albumId ->
                albumIdsCache.compute(albumArtist) { _, value ->
                    nNumberOfAlbums = (value?.size ?: 0) + 1
                    value?.apply { add(albumId) }
                        ?: ConcurrentSet(albumId)
                }
            }
            cache.compute(albumArtist) { _, value ->
                value?.apply {
                    numberOfAlbums = nNumberOfAlbums
                    numberOfTracks++
                } ?: run {
                    _all.update {
                        it + albumArtist
                    }
                    emitCount()
                    AlbumArtist(
                        name = albumArtist,
                        numberOfAlbums = 1,
                        numberOfTracks = 1,
                    )
                }
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

    fun sort(
        albumArtistNames: List<String>,
        by: AlbumArtistSortBy,
        reverse: Boolean,
    ): List<String> {
        val sorted = when (by) {
            AlbumArtistSortBy.CUSTOM -> albumArtistNames
            AlbumArtistSortBy.ARTIST_NAME -> albumArtistNames.sortedBy { get(it)?.name }
            AlbumArtistSortBy.TRACKS_COUNT -> albumArtistNames.sortedBy { get(it)?.numberOfTracks }
            AlbumArtistSortBy.ALBUMS_COUNT -> albumArtistNames.sortedBy { get(it)?.numberOfTracks }
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
