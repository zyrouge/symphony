package io.github.zyrouge.symphony.services.groove

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.Eventer
import io.github.zyrouge.symphony.utils.FuzzySearchOption
import io.github.zyrouge.symphony.utils.FuzzySearcher
import io.github.zyrouge.symphony.utils.subListNonStrict

enum class GenreSortBy {
    CUSTOM,
    GENRE,
    TRACKS_COUNT,
}

class GenreRepository(private val symphony: Symphony) {
    var isUpdating = false
    val onUpdate = Eventer<Nothing?>()

    private val searcher = FuzzySearcher<Genre>(
        options = listOf(FuzzySearchOption({ it.name }))
    )

    fun getAll() = symphony.groove.song.cachedGenres.values.toList()

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
