package io.github.zyrouge.symphony.utils

import androidx.annotation.FloatRange
import kotlin.math.max

data class FuzzySearchOption<T>(
    val value: (T) -> String?,
    val weight: Int = 1
)

data class FuzzyResultEntity<T>(
    @FloatRange(from = 0.0, to = 100.0) val ratio: Float,
    val entity: T
)

class FuzzySearcher<T>(val options: List<FuzzySearchOption<T>>) {
    fun search(terms: String, entities: List<T>) = entities
        .map { compare(terms, it) }
        .sortedByDescending { it.ratio }

    private fun compare(terms: String, entity: T): FuzzyResultEntity<T> {
        var ratio = 0f
        var totalWeight = 0
        options.forEach { option ->
            option.value(entity)?.let { value ->
                ratio += Fuzzy.compare(terms, value) * option.weight
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
