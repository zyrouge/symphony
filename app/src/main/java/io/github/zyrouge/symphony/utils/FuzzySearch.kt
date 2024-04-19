package io.github.zyrouge.symphony.utils

import kotlin.math.min

class FuzzySearchComparator(val input: String) {
    fun compareString(value: String) = Fuzzy.compare(input, value)

    fun compareCollection(values: Collection<String>): Int {
        if (values.isEmpty()) return 0
        var score = Int.MAX_VALUE
        values.forEach {
            score = min(score, compareString(it))
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
            .sortedBy { it.score }
        return when {
            maxLength > -1 -> results.subListNonStrict(maxLength)
            else -> results
        }
    }

    private fun compare(terms: String, entity: T): FuzzyResultEntity<T> {
        var score = Int.MAX_VALUE
        val comparator = FuzzySearchComparator(terms)
        options.forEach { option ->
            option.match.invoke(comparator, entity)?.let {
                score = min(score, it)
            }
        }
        return FuzzyResultEntity(score, entity)
    }
}

object Fuzzy {
    fun compare(input: String, against: String) =
        compareStrict(normalizeTerms(input), normalizeTerms(against))

    private const val MATCH_BONUS = 2f
    private const val DISTANCE_PENALTY_MULTIPLIER = 0.15f
    private const val NO_MATCH_PENALTY = -0.3f

    // Source: https://gist.github.com/ademar111190/34d3de41308389a0d0d8?permalink_comment_id=3664644#gistcomment-3664644
    private fun compareStrict(lhs: String, rhs: String): Int {
        val lhsLength = lhs.length + 1
        val rhsLength = rhs.length + 1
        var cost = Array(lhsLength) { it }
        var newCost = Array(lhsLength) { 0 }
        for (i in 1 until rhsLength) {
            newCost[0] = i
            for (j in 1 until lhsLength) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1
                newCost[j] = min(costInsert, costDelete).coerceAtMost(costReplace)
            }
            val swap = cost
            cost = newCost
            newCost = swap
        }
        return cost[lhsLength - 1]
    }

    private val whitespaceRegex = Regex("""\s+""")
    private fun normalizeTerms(terms: String) = terms.lowercase().replace(whitespaceRegex, " ")
}
