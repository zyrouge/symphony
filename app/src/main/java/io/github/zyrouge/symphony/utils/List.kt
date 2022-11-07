package io.github.zyrouge.symphony.utils

import kotlin.math.min

fun <T> List<T>.subListNonStrict(length: Int, start: Int = 0) =
    subList(start, min(start + length, size))

fun <T> MutableList<T>.swap(to: Collection<T>) {
    clear()
    addAll(to)
}
