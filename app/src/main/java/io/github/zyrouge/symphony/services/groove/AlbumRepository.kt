package io.github.zyrouge.symphony.services.groove

import android.content.ContentUris
import android.provider.MediaStore
import androidx.compose.runtime.mutableStateListOf
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.ui.helpers.createHandyImageRequest
import io.github.zyrouge.symphony.utils.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

enum class AlbumSortBy {
    CUSTOM,
    ALBUM_NAME,
    ARTIST_NAME,
    TRACKS_COUNT,
}

class AlbumRepository(private val symphony: Symphony) {
    private val cache = ConcurrentHashMap<Long, Album>()
    private val songIdsCache = ConcurrentHashMap<Long, ConcurrentSet<Long>>()
    private val searcher = FuzzySearcher<Long>(
        options = listOf(
            FuzzySearchOption({ get(it)?.name }, 3),
            FuzzySearchOption({ get(it)?.artist })
        )
    )

    val isUpdating get() = symphony.groove.mediaStore.isUpdating
    private val _all = mutableStateListOf<Long>()
    private val _allRapid = RapidMutableStateList(_all)
    val all = _all.asList()
    private val _count = MutableStateFlow(0)
    val count = _count.asStateFlow()

    private fun emitCount() = _count.tryEmit(cache.size)

    internal fun onSong(song: Song) {
        if (song.albumName == null || song.artistName == null) return
        songIdsCache.compute(song.albumId) { _, value ->
            value?.apply { add(song.id) } ?: ConcurrentSet(song.id)
        }
        cache.compute(song.albumId) { _, value ->
            value?.apply {
                numberOfTracks++
            } ?: run {
                _allRapid.add(song.albumId)
                emitCount()
                Album(
                    id = song.albumId,
                    name = song.albumName,
                    artist = song.artistName,
                    numberOfTracks = 1,
                )
            }
        }
    }

    internal fun onFinish() = _allRapid.sync()

    fun reset() {
        cache.clear()
        songIdsCache.clear()
        _all.clear()
        emitCount()
    }

    fun getDefaultArtworkUri() = Assets.getPlaceholderUri(symphony.applicationContext)

    fun getArtworkUri(albumId: Long) = ContentUris.withAppendedId(
        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
        albumId
    )

    fun createArtworkImageRequest(albumId: Long) = createHandyImageRequest(
        symphony.applicationContext,
        image = getArtworkUri(albumId),
        fallback = Assets.placeholderId,
    )

    fun search(albumIds: List<Long>, terms: String, limit: Int? = 7) = searcher
        .search(terms, albumIds)
        .subListNonStrict(limit ?: albumIds.size)

    fun sort(
        albumIds: List<Long>,
        by: AlbumSortBy,
        reverse: Boolean
    ): List<Long> {
        val sorted = when (by) {
            AlbumSortBy.CUSTOM -> albumIds
            AlbumSortBy.ALBUM_NAME -> albumIds.sortedBy { get(it)?.name }
            AlbumSortBy.ARTIST_NAME -> albumIds.sortedBy { get(it)?.artist }
            AlbumSortBy.TRACKS_COUNT -> albumIds.sortedBy { get(it)?.numberOfTracks }
        }
        return if (reverse) sorted.reversed() else sorted
    }

    fun count() = cache.size
    fun ids() = cache.keys.toList()
    fun values() = cache.values.toList()

    fun get(id: Long) = cache[id]
    fun get(ids: List<Long>) = ids.mapNotNull { get(it) }.toList()
    fun getSongIds(albumId: Long) = songIdsCache[albumId]?.toList() ?: emptyList()
}
