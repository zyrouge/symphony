package io.github.zyrouge.symphony.services.groove.repositories

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.AlbumArtist
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.ui.helpers.createHandyImageRequest
import io.github.zyrouge.symphony.utils.ConcurrentSet
import io.github.zyrouge.symphony.utils.FuzzySearchOption
import io.github.zyrouge.symphony.utils.FuzzySearcher
import io.github.zyrouge.symphony.utils.SearchProvider
import io.github.zyrouge.symphony.utils.Searcher
import io.github.zyrouge.symphony.utils.concurrentSetOf
import io.github.zyrouge.symphony.utils.withCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.ConcurrentHashMap

class AlbumArtistRepository(private val symphony: Symphony) {
    enum class SortBy {
        CUSTOM,
        ARTIST_NAME,
        TRACKS_COUNT,
        ALBUMS_COUNT,
    }

    private val cache = ConcurrentHashMap<String, AlbumArtist>()
    private val songIdsCache = ConcurrentHashMap<String, ConcurrentSet<String>>()
    private val albumIdsCache = ConcurrentHashMap<String, ConcurrentSet<String>>()
    private val searcher = when (symphony.settings.searchProvider.value) {
        SearchProvider.Fuzzy -> FuzzySearcher<String>(
            options = listOf(FuzzySearchOption({ v -> get(v)?.name?.let { compareString(it) } }))
        )

        SearchProvider.Normal -> Searcher<String>(
            listOf { v -> get(v)?.name ?: "" }
        )
    }

    val isUpdating get() = symphony.groove.exposer.isUpdating
    private val _all = MutableStateFlow<List<String>>(emptyList())
    val all = _all.asStateFlow()
    private val _count = MutableStateFlow(0)
    val count = _count.asStateFlow()

    private fun emitCount() = _count.update {
        cache.size
    }

    internal fun onSong(song: Song) {
        song.albumArtists.forEach { albumArtist ->
            songIdsCache.compute(albumArtist) { _, value ->
                value?.apply { add(song.id) } ?: concurrentSetOf(song.id)
            }
            var nNumberOfAlbums = 0
            symphony.groove.album.getIdFromSong(song)?.let { albumId ->
                albumIdsCache.compute(albumArtist) { _, value ->
                    nNumberOfAlbums = (value?.size ?: 0) + 1
                    value?.apply { add(albumId) } ?: concurrentSetOf(albumId)
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
