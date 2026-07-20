package io.github.zyrouge.symphony.utils

object StringSorter {
    enum class SortBy {
        CUSTOM,
        NAME,
    }

    fun sort(values: List<String>, by: SortBy, reverse: Boolean): List<String> {
        val sorted = when (by) {
            SortBy.CUSTOM -> values
            SortBy.NAME -> values.sorted()
        }
        return if (reverse) sorted.reversed() else sorted
    }
}
