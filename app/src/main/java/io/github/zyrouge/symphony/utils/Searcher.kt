package io.github.zyrouge.symphony.utils

import java.util.stream.Collectors

enum class SearchProvider {
    Normal,
    Fuzzy
}

interface ISearchProvider<T> {
    fun search(
        terms: String,
        entities: List<T>,
        maxLength: Int = -1,
    ) : List<SearchResultEntity<T>>
}

class Searcher<T>(private var stringGetters: List<(T) -> String>) : ISearchProvider<T> {
    override fun search(
        terms: String,
        entities: List<T>,
        maxLength: Int,
    ): List<SearchResultEntity<T>> {

        val results = entities.stream()
            .filter { match(terms, it) }
            .map { SearchResultEntity(terms.length - it.toString().length, it ) } //smaller names benefit
            .sorted(Comparator.comparing { it.score })
            .collect(Collectors.toList())

        return when {
            maxLength > -1 -> results.subListNonStrict(maxLength)
            else -> results
        }
    }

    private fun match(terms: String, entity: T) =
        stringGetters
            .map { it(entity) }
            .any { it.contains(terms, true) }
}