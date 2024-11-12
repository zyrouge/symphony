package io.github.zyrouge.symphony.services.groove.repositories

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.ui.helpers.createHandyImageRequest
import io.github.zyrouge.symphony.utils.ConcurrentSet
import io.github.zyrouge.symphony.utils.FuzzySearchOption
import io.github.zyrouge.symphony.utils.FuzzySearcher
import io.github.zyrouge.symphony.utils.concurrentSetOf
import io.github.zyrouge.symphony.utils.joinToStringIfNotEmpty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.ConcurrentHashMap

class AlbumRepository(private val symphony: Symphony) {
    enum class SortBy {
        CUSTOM,
        ALBUM_NAME,
        ARTIST_NAME,
        TRACKS_COUNT,
    }

    private val cache = ConcurrentHashMap<String, Album>()
    private val songIdsCache = ConcurrentHashMap<String, ConcurrentSet<String>>()
    private val searcher = FuzzySearcher<String>(
        options = listOf(
            FuzzySearchOption({ v -> get(v)?.name?.let { compareString(it) } }, 3),
            FuzzySearchOption({ v -> get(v)?.artists?.let { compareCollection(it) } })
        )
    )

    val isUpdating get() = symphony.groove.exposer.isUpdating
    private val _all = MutableStateFlow<List<String>>(emptyList())
    val all = _all.asStateFlow()
    private val _count = MutableStateFlow(0)
    val count = _count.asStateFlow()

    private fun emitCount() = _count.update {
        cache.size
    }

    internal fun onSong(song: Song) {
        val albumId = getIdFromSong(song) ?: return
        songIdsCache.compute(albumId) { _, value ->
            value?.apply { add(song.id) } ?: concurrentSetOf(song.id)
        }
        cache.compute(albumId) { _, value ->
            value?.apply {
                artists.addAll(song.artists)
                numberOfTracks++
            } ?: run {
                _all.update {
                    it + albumId
                }
                emitCount()
                Album(
                    id = albumId,
                    name = song.album!!,
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

    fun getIdFromSong(song: Song): String? {
        if (song.album == null) return null
        val artists = song.albumArtists.sorted().joinToString("-")
        return "${song.album}-${artists}"
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

    fun sort(albumIds: List<String>, by: SortBy, reverse: Boolean): List<String> {
        val sorted = when (by) {
            SortBy.CUSTOM -> albumIds
            SortBy.ALBUM_NAME -> albumIds.sortedBy { get(it)?.name }
            SortBy.ARTIST_NAME -> albumIds.sortedBy { get(it)?.artists?.joinToStringIfNotEmpty() }
            SortBy.TRACKS_COUNT -> albumIds.sortedBy { get(it)?.numberOfTracks }
        }
        return if (reverse) sorted.reversed() else sorted
    }

    fun count() = cache.size
    fun ids() = cache.keys.toList()
    fun values() = cache.values.toList()

    fun get(albumId: String) = cache[albumId]
    fun get(albumIds: List<String>) = albumIds.mapNotNull { get(it) }.toList()
    fun getSongIds(albumId: String) = songIdsCache[albumId]?.toList() ?: emptyList()
}
