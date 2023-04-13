package io.github.zyrouge.symphony.services.groove

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.ui.helpers.createHandyImageRequest
import io.github.zyrouge.symphony.utils.*
import java.util.concurrent.ConcurrentHashMap

enum class ArtistSortBy {
    CUSTOM,
    ARTIST_NAME,
    TRACKS_COUNT,
    ALBUMS_COUNT,
}

class ArtistRepository(private val symphony: Symphony) {
    val cache = ConcurrentHashMap<String, Artist>()
    val songIdsCache = ConcurrentHashMap<String, ConcurrentSet<Long>>()
    val albumIdsCache = ConcurrentHashMap<String, ConcurrentSet<Long>>()
    var isUpdating = false
    val onUpdate = Eventer.nothing()
    val onUpdateRapidDispatcher = GrooveEventerRapidUpdateDispatcher(onUpdate)

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
        onUpdate.dispatch()
    }

    private fun onSong(song: Song) {
        if (song.artistName == null) return
        songIdsCache.compute(song.artistName) { _, value ->
            value?.apply { add(song.id) }
                ?: ConcurrentSet(song.id)
        }
        var nNumberOfAlbums = 0
        albumIdsCache.compute(song.artistName) { _, value ->
            nNumberOfAlbums = (value?.size ?: 0) + 1
            value?.apply { add(song.albumId) }
                ?: ConcurrentSet(song.albumId)
        }
        cache.compute(song.artistName) { _, value ->
            value?.apply {
                numberOfAlbums = nNumberOfAlbums
                numberOfTracks++
            } ?: Artist(
                name = song.artistName,
                numberOfAlbums = 1,
                numberOfTracks = 1,
            )
        }
        onUpdateRapidDispatcher.dispatch()
    }

    fun reset() {
        cache.clear()
        onUpdate.dispatch()
    }

    fun getArtistArtworkUri(artistName: String) = albumIdsCache[artistName]?.firstOrNull()
        ?.let { symphony.groove.album.getAlbumArtworkUri(it) }
        ?: symphony.groove.album.getDefaultAlbumArtworkUri()

    fun createArtistArtworkImageRequest(artistName: String) = createHandyImageRequest(
        symphony.applicationContext,
        image = getArtistArtworkUri(artistName),
        fallback = Assets.placeholderId,
    )

    fun getAll() = cache.values.toList()
    fun getArtistFromArtistName(artistName: String) = cache[artistName]

    fun getAlbumIdsOfArtistName(artistName: String) = albumIdsCache[artistName] ?: listOf()
    fun getAlbumsOfArtistName(artistName: String) = getAlbumIdsOfArtistName(artistName)
        .mapNotNull { symphony.groove.album.getAlbumWithId(it) }

    fun getSongIdsOfArtistName(artistName: String) = songIdsCache[artistName]?.toList() ?: listOf()
    fun getSongsOfArtistName(artistName: String) = getSongIdsOfArtistName(artistName)
        .mapNotNull { symphony.groove.song.getSongWithId(it) }

    companion object {
        val searcher = FuzzySearcher<Artist>(
            options = listOf(
                FuzzySearchOption({ it.name })
            )
        )

        fun search(artists: List<Artist>, terms: String, limit: Int? = 7) = searcher
            .search(terms, artists)
            .subListNonStrict(limit ?: artists.size)

        fun sort(artists: List<Artist>, by: ArtistSortBy, reversed: Boolean): List<Artist> {
            val sorted = when (by) {
                ArtistSortBy.CUSTOM -> artists.toList()
                ArtistSortBy.ARTIST_NAME -> artists.sortedBy { it.name }
                ArtistSortBy.TRACKS_COUNT -> artists.sortedBy { it.numberOfTracks }
                ArtistSortBy.ALBUMS_COUNT -> artists.sortedBy { it.numberOfTracks }
            }
            return if (reversed) sorted.reversed() else sorted
        }
    }
}
