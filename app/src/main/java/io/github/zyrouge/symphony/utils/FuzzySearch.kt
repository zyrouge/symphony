package io.github.zyrouge.symphony.utils

import me.xdrop.fuzzywuzzy.FuzzySearch
import kotlin.math.max

class FuzzySearchComparator(val input: String) {
    fun compareString(value: String) = Fuzzy.compare(input, value)

    fun compareCollection(values: Collection<String>): Int? {
        if (values.isEmpty()) return null
        var score = 0
        values.forEach {
            score = max(score, compareString(it))
        }
        return score
    }
}

data class FuzzySearchOption<T>(
    val match: FuzzySearchComparator.(T) -> Int?,
    val weight: Int = 1,
)

data class FuzzyResultEntity<T>(
    val score: Int,
    val entity: T,
)

class FuzzySearcher<T>(val options: List<FuzzySearchOption<T>>) {
    fun search(
        terms: String,
        entities: List<T>,
        maxLength: Int = -1,
    ): List<FuzzyResultEntity<T>> {
        val results = entities
            .map { compare(terms, it) }
            .sortedByDescending { it.score }
        return when {
            maxLength > -1 -> results.subListNonStrict(maxLength)
            else -> results
        }
    }

    private fun compare(terms: String, entity: T): FuzzyResultEntity<T> {
        var score = 0
        val comparator = FuzzySearchComparator(terms)
        options.forEach { option ->
            option.match.invoke(comparator, entity)?.let {
                score = max(score, it * option.weight)
            }
        }
        return FuzzyResultEntity(score, entity)
    }
}

object Fuzzy {
    fun compare(input: String, against: String) = FuzzySearch.tokenSetPartialRatio(
        normalizeTerms(input),
        normalizeTerms(against),
    )

    private val whitespaceRegex = Regex("""\s+""")
    private val alphaNumericRegex = Regex("""[^A-Za-z0-9]""")
    private fun normalizeTerms(terms: String) = terms.lowercase()
        .replace(whitespaceRegex, " ")
        .replace(alphaNumericRegex, "")
}
