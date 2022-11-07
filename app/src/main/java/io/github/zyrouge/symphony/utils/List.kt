package io.github.zyrouge.symphony.utils

import kotlin.math.min

fun <T> List<T>.subListNonStrict(length: Int, start: Int = 0) =
    subList(start, min(start + length, size))

fun <T> MutableList<T>.swap(to: Collection<T>) {
    clear()
    addAll(to)
}

//data class IndexedShuffled<T>(
//    val originalIndices: List<Int>,
//    val shuffled: List<T>,
//)
//
//fun <T> List<T>.indexedShuffle(): IndexedShuffled<T> {
//    val cloned = toMutableList()
//    val indices = mutableListOf<Int>()
//    val shuffled = mutableListOf<T>()
//    while (cloned.isNotEmpty()) {
//        val index = Random.nextInt(cloned.size)
//        shuffled.add(cloned[index])
//        indices.add(index)
//        cloned.removeAt(index)
//    }
//    return IndexedShuffled(
//        originalIndices = indices,
//        shuffled = shuffled,
//    )
//}
