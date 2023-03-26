package io.github.zyrouge.symphony.services.groove

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.ui.helpers.createHandyImageRequest
import io.github.zyrouge.symphony.utils.*
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
    var isUpdating = false
    val onUpdate = Eventer.nothing()

    private val searcher = FuzzySearcher<AlbumArtist>(
        options = listOf(
            FuzzySearchOption({ it.name })
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
        onUpdate.dispatch()
    }

    fun reset() {
        cache.clear()
        onUpdate.dispatch()
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

    fun search(terms: String) = searcher.search(terms, getAll()).subListNonStrict(7)

    companion object {
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
