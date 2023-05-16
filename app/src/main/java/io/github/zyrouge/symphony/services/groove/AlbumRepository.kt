package io.github.zyrouge.symphony.services.groove

import android.content.ContentUris
import android.provider.MediaStore
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

enum class AlbumSortBy {
    CUSTOM,
    ALBUM_NAME,
    ARTIST_NAME,
    TRACKS_COUNT,
}

class AlbumRepository(private val symphony: Symphony) {
    val cache = ConcurrentHashMap<Long, Album>()
    val songIdsCache = ConcurrentHashMap<Long, ConcurrentSet<Long>>()

    val isUpdating get() = symphony.groove.mediaStore.isUpdating
    private val _all = MutableStateFlow<List<Long>>(listOf())
    val all = _all.asStateFlow()

    private fun emitAll() = _all.tryEmit(cache.keys.toList())

    internal fun onSong(song: Song) {
        if (song.albumName == null || song.artistName == null) return
        songIdsCache.compute(song.albumId) { _, value ->
            value?.apply { add(song.id) } ?: ConcurrentSet(song.id)
        }
        cache.compute(song.albumId) { _, value ->
            value?.apply {
                numberOfTracks++
            } ?: Album(
                id = song.albumId,
                name = song.albumName,
                artist = song.artistName,
                numberOfTracks = 1,
            )
        }
        emitAll()
    }

    fun reset() {
        cache.clear()
        songIdsCache.clear()
        emitAll()
    }

    fun getDefaultAlbumArtworkUri() = Assets.getPlaceholderUri(symphony.applicationContext)

    fun getAlbumArtworkUri(albumId: Long) = ContentUris.withAppendedId(
        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
        albumId
    )

    fun createAlbumArtworkImageRequest(albumId: Long) = createHandyImageRequest(
        symphony.applicationContext,
        image = getAlbumArtworkUri(albumId),
        fallback = Assets.placeholderId,
    )

    fun count() = cache.size
    fun getAll() = cache.values.toList()
    fun getAlbumWithId(albumId: Long) = cache[albumId]

    fun getSongIdsOfAlbumId(albumId: Long) = songIdsCache[albumId]?.toList() ?: listOf()
    fun getSongsOfAlbumId(albumId: Long) = getSongIdsOfAlbumId(albumId)
        .mapNotNull { symphony.groove.song.getSongWithId(it) }

    companion object {
        val searcher = FuzzySearcher<Album>(
            options = listOf(
                FuzzySearchOption({ it.name }, 3),
                FuzzySearchOption({ it.artist })
            )
        )

        fun search(albums: List<Album>, terms: String, limit: Int? = 7) = searcher
            .search(terms, albums)
            .subListNonStrict(limit ?: albums.size)

        fun sort(albums: List<Album>, by: AlbumSortBy, reversed: Boolean): List<Album> {
            val sorted = when (by) {
                AlbumSortBy.CUSTOM -> albums.toList()
                AlbumSortBy.ALBUM_NAME -> albums.sortedBy { it.name }
                AlbumSortBy.ARTIST_NAME -> albums.sortedBy { it.artist }
                AlbumSortBy.TRACKS_COUNT -> albums.sortedBy { it.numberOfTracks }
            }
            return if (reversed) sorted.reversed() else sorted
        }
    }
}
