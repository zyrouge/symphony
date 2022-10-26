package io.github.zyrouge.symphony.utils

import androidx.annotation.FloatRange
import me.xdrop.fuzzywuzzy.FuzzySearch

data class FuzzySearchOption<T>(
    val value: (T) -> String?,
    val weight: Int = 1
)

data class FuzzyResultEntity<T>(
    @FloatRange(from = 0.0, to = 100.0) val ratio: Float,
    val entity: T
)

class FuzzySearcher<T>(val options: List<FuzzySearchOption<T>>) {
    fun search(
        terms: String,
        entities: List<T>,
        @FloatRange(from = 0.0, to = 100.0) minimumRatio: Float = 0f
    ): List<FuzzyResultEntity<T>> {
        val matches = mutableListOf<FuzzyResultEntity<T>>()
        entities.forEach {
            val match = compare(terms, it)
            if (match.ratio >= minimumRatio) {
                matches.add(match)
            }
        }
        return matches.sortedByDescending { it.ratio }
    }

    private fun compare(terms: String, entity: T): FuzzyResultEntity<T> {
        var ratio = 0f
        var totalWeight = 0
        options.forEach { option ->
            option.value(entity)?.let { value ->
                ratio += FuzzySearch.partialRatio(terms, value) * option.weight
                totalWeight += option.weight
            }
        }
        return FuzzyResultEntity(ratio / totalWeight, entity)
    }
}