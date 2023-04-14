package io.github.zyrouge.symphony.services.groove

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.*
import java.util.concurrent.ConcurrentHashMap

enum class GenreSortBy {
    CUSTOM,
    GENRE,
    TRACKS_COUNT,
}

class GenreRepository(private val symphony: Symphony) {
    val cache = ConcurrentHashMap<String, Genre>()
    val songIdsCache = ConcurrentHashMap<String, ConcurrentSet<Long>>()
    var isUpdating = false
    val onUpdateStart = Eventer.nothing()
    val onUpdate = Eventer.nothing()
    val onUpdateEnd = Eventer.nothing()
    val onUpdateRapidDispatcher = GrooveEventerRapidUpdateDispatcher(onUpdate)

    fun ready() {
        symphony.groove.mediaStore.onSong.subscribe { onSong(it) }
        symphony.groove.mediaStore.onFetchStart.subscribe { onFetchStart() }
        symphony.groove.mediaStore.onFetchEnd.subscribe { onFetchEnd() }
    }

    private fun onFetchStart() {
        isUpdating = true
        onUpdateStart.dispatch()
    }

    private fun onFetchEnd() {
        isUpdating = false
        onUpdateEnd.dispatch()
    }

    private fun onSong(song: Song) {
        if (song.additional.genre == null) return
        songIdsCache.compute(song.additional.genre) { _, value ->
            value?.apply { add(song.id) }
                ?: ConcurrentSet(song.id)
        }
        cache.compute(song.additional.genre) { _, value ->
            value?.apply {
                numberOfTracks++
            } ?: Genre(
                name = song.additional.genre,
                numberOfTracks = 1,
            )
        }
        onUpdateRapidDispatcher.dispatch()
    }

    fun reset() {
        cache.clear()
        songIdsCache.clear()
        onUpdate.dispatch()
    }

    fun count() = cache.size
    fun getAll() = cache.values.toList()

    fun getSongIdsOfGenre(genre: String) = songIdsCache[genre] ?: listOf()
    fun getSongsOfGenre(genre: String) = getSongIdsOfGenre(genre)
        .mapNotNull { symphony.groove.song.getSongWithId(it) }

    companion object {
        val searcher = FuzzySearcher<Genre>(
            options = listOf(FuzzySearchOption({ it.name }))
        )

        fun search(genres: List<Genre>, terms: String, limit: Int? = 7) = searcher
            .search(terms, genres)
            .subListNonStrict(limit ?: genres.size)

        fun sort(genres: List<Genre>, by: GenreSortBy, reversed: Boolean): List<Genre> {
            val sorted = when (by) {
                GenreSortBy.CUSTOM -> genres.toList()
                GenreSortBy.GENRE -> genres.sortedBy { it.name }
                GenreSortBy.TRACKS_COUNT -> genres.sortedBy { it.numberOfTracks }
            }
            return if (reversed) sorted.reversed() else sorted
        }
    }
}
