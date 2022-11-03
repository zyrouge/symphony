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
    fun search(terms: String, entities: List<T>): List<FuzzyResultEntity<T>> {
        return entities
            .map { compare(terms, it) }
            .sortedByDescending { it.ratio }
    }

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
        compareStrict(input.lowercase(), against.lowercase())

    private fun compareStrict(input: String, against: String): Float {
        val inputLetters = input.split("")
        val inputLength = inputLetters.size
        val againstLetters = against.split("")
        val againstLength = againstLetters.size
        var pos = 0
        var score = 0f
        for (i in 0 until inputLength) {
            val x = inputLetters[i]
            var matched = false
            for (j in pos until againstLength) {
                val y = againstLetters[j]
                if (x == y) {
                    pos = j + 1
                    matched = true
                    break
                }
            }
            score += if (matched) 1f else -0.3f
        }
        return max(0f, score) / againstLength
    }
}
