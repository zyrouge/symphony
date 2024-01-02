package io.github.zyrouge.symphony.utils

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

fun <T> List<T>.subListNonStrict(length: Int, start: Int = 0) =
    subList(start, min(start + length, size))

fun <T> List<T>.randomSubList(length: Int): List<T> {
    val mut = toMutableList()
    val out = mutableListOf<T>()
    val possibleLength = max(0, min(length, mut.size))
    for (i in 0 until possibleLength) {
        val index = Random.nextInt(mut.size)
        out.add(mut.removeAt(index))
    }
    return out
}

fun <T> List<T>.indexOfOrNull(value: T) = indexOfOrNull { it == value }
fun <T> List<T>.indexOfOrNull(predicate: (T) -> Boolean): Int? {
    for (i in indices) {
        if (predicate(get(i))) return i
    }
    return null
}

fun <T> List<T>.mutate(fn: MutableList<T>.() -> Unit): List<T> {
    val out = toMutableList()
    fn.invoke(out)
    return out
}

fun List<String>.joinToStringIfNotEmpty() = if (isNotEmpty()) joinToString() else null

class ConcurrentList<T> : MutableList<T> {
    private val list = mutableListOf<T>()
    private val lock = ReentrantReadWriteLock()

    override val size: Int get() = lock.read { list.size }

    override operator fun set(index: Int, element: T) = lock.write { list.set(index, element) }
    override operator fun get(index: Int) = lock.read { list[index] }

    override fun contains(element: T) = lock.read { list.contains(element) }
    override fun containsAll(elements: Collection<T>) = lock.read { list.containsAll(elements) }
    override fun indexOf(element: T) = lock.read { list.indexOf(element) }
    override fun lastIndexOf(element: T) = lock.read { list.lastIndexOf(element) }
    override fun isEmpty() = lock.read { list.isEmpty() }
    override fun subList(fromIndex: Int, toIndex: Int) =
        lock.read { list.subList(fromIndex, toIndex) }

    override fun add(element: T) = lock.write { list.add(element) }
    override fun add(index: Int, element: T) = lock.write { list.add(index, element) }
    override fun addAll(elements: Collection<T>) = lock.write { list.addAll(elements) }
    override fun addAll(index: Int, elements: Collection<T>) =
        lock.write { list.addAll(index, elements) }

    override fun clear() = lock.write { list.clear() }
    override fun remove(element: T) = lock.write { list.remove(element) }
    override fun removeAll(elements: Collection<T>) = lock.write { list.removeAll(elements) }
    override fun removeAt(index: Int) = lock.write { list.removeAt(index) }
    override fun retainAll(elements: Collection<T>) = lock.write { list.retainAll(elements) }

    // NOTE: `write` lock since it returns `MutableIterator`s
    override fun iterator() = lock.write { list.iterator() }
    override fun listIterator() = lock.write { list.listIterator() }
    override fun listIterator(index: Int) = lock.write { list.listIterator(index) }
}

