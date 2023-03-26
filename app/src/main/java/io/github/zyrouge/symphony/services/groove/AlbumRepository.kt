package io.github.zyrouge.symphony.services.groove

import android.content.ContentUris
import android.provider.MediaStore
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.ui.helpers.createHandyImageRequest
import io.github.zyrouge.symphony.utils.*
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
    var isUpdating = false
    val onUpdate = Eventer.nothing()

    private val searcher = FuzzySearcher<Album>(
        options = listOf(
            FuzzySearchOption({ it.name }, 3),
            FuzzySearchOption({ it.artist })
        )
    )

    fun ready() {
        symphony.groove.mediaStore.onSong.subscribe { onSong(it) }
        symphony.groove.mediaStore.onFetchStart.subscribe { onFetchStart() }
        symphony.groove.mediaStore.onFetchEnd.subscribe { onFetchEnd() }
    }

    private fun onFetchStart() {
        isUpdating = true
    }

    private fun onFetchEnd() {
        isUpdating = false
    }

    private fun onSong(song: Song) {
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
        onUpdate.dispatch()
    }

    fun reset() {
        cache.clear()
        songIdsCache.clear()
        onUpdate.dispatch()
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

    fun getAll() = cache.values.toList()
    fun getAlbumWithId(albumId: Long) = cache[albumId]

    fun getSongIdsOfAlbumId(albumId: Long) = songIdsCache[albumId]?.toList() ?: listOf()
    fun getSongsOfAlbumId(albumId: Long) = getSongIdsOfAlbumId(albumId)
        .mapNotNull { symphony.groove.song.getSongWithId(it) }

    fun search(terms: String) = searcher.search(terms, getAll()).subListNonStrict(7)

    companion object {
        fun sort(songs: List<Album>, by: AlbumSortBy, reversed: Boolean): List<Album> {
            val sorted = when (by) {
                AlbumSortBy.CUSTOM -> songs.toList()
                AlbumSortBy.ALBUM_NAME -> songs.sortedBy { it.name }
                AlbumSortBy.ARTIST_NAME -> songs.sortedBy { it.artist }
                AlbumSortBy.TRACKS_COUNT -> songs.sortedBy { it.numberOfTracks }
            }
            return if (reversed) sorted.reversed() else sorted
        }
    }
}
