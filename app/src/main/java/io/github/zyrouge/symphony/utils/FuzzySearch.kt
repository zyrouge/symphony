package io.github.zyrouge.symphony.utils

import androidx.annotation.FloatRange
import kotlin.math.max
import kotlin.math.min

class FuzzySearchComparator(val input: String) {
    fun compareString(value: String) = Fuzzy.compare(input, value)

    fun compareCollection(values: Collection<String>): Float {
        var weight = 0f
        values.forEach {
            weight += compareString(it)
        }
        return weight / min(1, values.size)
    }
}

data class FuzzySearchOption<T>(
    val match: FuzzySearchComparator.(T) -> Float?,
    val weight: Int = 1,
)

data class FuzzyResultEntity<T>(
    @FloatRange(from = 0.0, to = 100.0) val ratio: Float,
    val entity: T,
)

class FuzzySearcher<T>(val options: List<FuzzySearchOption<T>>) {
    fun search(
        terms: String,
        entities: List<T>,
        maxLength: Int = -1,
        minScore: Float = 0.25f,
    ): List<FuzzyResultEntity<T>> {
        var results = entities
            .map { compare(terms, it) }
            .sortedByDescending { it.ratio }
        if (maxLength > -1) {
            results = results.subListNonStrict(maxLength)
        }
        return results.filter { it.ratio > minScore }
    }

    private fun compare(terms: String, entity: T): FuzzyResultEntity<T> {
        var ratio = 0f
        var totalWeight = 0
        val comparator = FuzzySearchComparator(terms)
        options.forEach { option ->
            option.match.invoke(comparator, entity)?.let {
                ratio += it * option.weight
                totalWeight += option.weight
            }
        }
        return FuzzyResultEntity(ratio / totalWeight, entity)
    }
}

object Fuzzy {
    fun compare(input: String, against: String) =
        compareStrict(normalizeTerms(input), normalizeTerms(against))

    private const val MATCH_BONUS = 2f
    private const val DISTANCE_PENALTY_MULTIPLIER = 0.15f
    private const val NO_MATCH_PENALTY = -0.3f

    private fun compareStrict(input: String, against: String): Float {
        val inputLength = input.length
        val againstLength = against.length
        var currPosition = 0
        var prevPosition = 0
        var score = 0f
        for (i in 0 until inputLength) {
            val x = input[i]
            var matched = false
            for (j in currPosition until againstLength) {
                val y = against[j]
                if (x == y) {
                    prevPosition = currPosition
                    currPosition = j
                    matched = true
                    break
                }
            }
            score += if (matched) MATCH_BONUS - (DISTANCE_PENALTY_MULTIPLIER * (currPosition - prevPosition - 1))
            else NO_MATCH_PENALTY
            currPosition++
        }
        return max(0f, score) / againstLength
    }

    private val whitespaceRegex = Regex("""\s+""")
    private fun normalizeTerms(terms: String) = terms.lowercase().replace(whitespaceRegex, " ")
}
