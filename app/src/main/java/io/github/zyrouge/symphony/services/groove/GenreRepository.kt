package io.github.zyrouge.symphony.services.groove

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.Eventer
import io.github.zyrouge.symphony.utils.FuzzySearchOption
import io.github.zyrouge.symphony.utils.FuzzySearcher
import io.github.zyrouge.symphony.utils.subListNonStrict

class GenreRepository(private val symphony: Symphony) {
    var isUpdating = false
    val onUpdate = Eventer<Nothing?>()

    private val searcher = FuzzySearcher<String>(
        options = listOf(FuzzySearchOption({ it }))
    )

    fun getAll() = symphony.groove.song.cachedGenres.toList()

    fun search(terms: String) = searcher.search(terms, getAll()).subListNonStrict(7)
}