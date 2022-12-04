package io.github.zyrouge.symphony.utils

import kotlin.math.min
import kotlin.random.Random

fun <T> List<T>.subListNonStrict(length: Int, start: Int = 0) =
    subList(start, min(start + length, size))

fun <T> MutableList<T>.swap(to: Collection<T>) {
    with(this) {
        clear()
        addAll(to)
    }
}

fun <T> List<T>.randomSubList(length: Int) = List(length) { get(Random.nextInt(size)) }

fun <T> List<T>.strictEquals(to: List<T>): Boolean {
    if (size != to.size) return false
    for (i in indices) {
        if (get(i) != to[i]) return false
    }
    return true
}

fun <T> List<T>.indexOfOrNull(value: T) = indexOfOrNull { it == value }
fun <T> List<T>.indexOfOrNull(predicate: (T) -> Boolean): Int? {
    for (i in indices) {
        if (predicate(get(i))) return i
    }
    return null
}
