package io.github.zyrouge.symphony.services.groove

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.ui.helpers.createHandyImageRequest
import io.github.zyrouge.symphony.utils.ConcurrentSet
import io.github.zyrouge.symphony.utils.FuzzySearchOption
import io.github.zyrouge.symphony.utils.FuzzySearcher
import io.github.zyrouge.symphony.utils.joinToStringIfNotEmpty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.ConcurrentHashMap

enum class AlbumSortBy {
    CUSTOM,
    ALBUM_NAME,
    ARTIST_NAME,
    TRACKS_COUNT,
}

class AlbumRepository(private val symphony: Symphony) {
    private val cache = ConcurrentHashMap<String, Album>()
    private val songIdsCache = ConcurrentHashMap<String, ConcurrentSet<Long>>()
    private val searcher = FuzzySearcher<String>(
        options = listOf(
            FuzzySearchOption({ v -> get(v)?.name?.let { compareString(it) } }, 3),
            FuzzySearchOption({ v -> get(v)?.artists?.let { compareCollection(it) } })
        )
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
        if (song.album == null) return
        songIdsCache.compute(song.album) { _, value ->
            value?.apply { add(song.id) } ?: ConcurrentSet(song.id)
        }
        cache.compute(song.album) { _, value ->
            value?.apply {
                artists.addAll(song.artists)
                numberOfTracks++
            } ?: run {
                _all.update {
                    it + song.album
                }
                emitCount()
                Album(
                    name = song.album,
                    artists = song.artists.toMutableSet(),
                    numberOfTracks = 1,
                )
            }
        }
    }

    internal fun onFinish() {}

    fun reset() {
        cache.clear()
        songIdsCache.clear()
        _all.update {
            emptyList()
        }
        emitCount()
    }

    fun getArtworkUri(albumName: String) = songIdsCache[albumName]?.firstOrNull()
        ?.let { symphony.groove.song.getArtworkUri(it) }
        ?: symphony.groove.song.getDefaultArtworkUri()

    fun createArtworkImageRequest(albumName: String) = createHandyImageRequest(
        symphony.applicationContext,
        image = getArtworkUri(albumName),
        fallback = Assets.placeholderDarkId,
    )

    fun search(albumNames: List<String>, terms: String, limit: Int = 7) = searcher
        .search(terms, albumNames, maxLength = limit)

    fun sort(
        albumNames: List<String>,
        by: AlbumSortBy,
        reverse: Boolean,
    ): List<String> {
        val sorted = when (by) {
            AlbumSortBy.CUSTOM -> albumNames
            AlbumSortBy.ALBUM_NAME -> albumNames.sortedBy { get(it)?.name }
            AlbumSortBy.ARTIST_NAME -> albumNames.sortedBy { get(it)?.artists?.joinToStringIfNotEmpty() }
            AlbumSortBy.TRACKS_COUNT -> albumNames.sortedBy { get(it)?.numberOfTracks }
        }
        return if (reverse) sorted.reversed() else sorted
    }

    fun count() = cache.size
    fun ids() = cache.keys.toList()
    fun values() = cache.values.toList()

    fun get(albumName: String) = cache[albumName]
    fun get(albumNames: List<String>) = albumNames.mapNotNull { get(it) }.toList()
    fun getSongIds(albumName: String) = songIdsCache[albumName]?.toList() ?: emptyList()
}
