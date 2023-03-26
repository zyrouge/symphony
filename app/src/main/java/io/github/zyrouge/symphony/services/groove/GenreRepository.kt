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
    val onUpdate = Eventer.nothing()

    private val searcher = FuzzySearcher<Genre>(
        options = listOf(FuzzySearchOption({ it.name }))
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
        onUpdate.dispatch()
    }

    fun reset() {
        cache.clear()
        songIdsCache.clear()
        onUpdate.dispatch()
    }

    fun getAll() = cache.values.toList()

    fun getSongIdsOfGenre(genre: String) = songIdsCache[genre] ?: listOf()
    fun getSongsOfGenre(genre: String) = getSongIdsOfGenre(genre)
        .mapNotNull { symphony.groove.song.getSongWithId(it) }

    fun search(terms: String) = searcher.search(terms, getAll()).subListNonStrict(7)

    companion object {
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
