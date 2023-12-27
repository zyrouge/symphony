package io.github.zyrouge.symphony.utils

import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

fun Set<String>.joinToStringIfNotEmpty() = if (isNotEmpty()) joinToString() else null

class ConcurrentSet<T>(vararg elements: T) : MutableSet<T> {
    private val set = Collections.newSetFromMap(ConcurrentHashMap<T, Boolean>()).apply {
        addAll(elements)
    }

    override val size: Int get() = set.size
    override fun contains(element: T) = set.contains(element)
    override fun containsAll(elements: Collection<T>) = set.containsAll(elements)
    override fun isEmpty() = set.isEmpty()
    override fun add(element: T) = set.add(element)
    override fun addAll(elements: Collection<T>) = set.addAll(elements)
    override fun clear() = set.clear()
    override fun remove(element: T) = set.remove(element)
    override fun removeAll(elements: Collection<T>) = set.removeAll(elements.toSet())
    override fun retainAll(elements: Collection<T>) = set.retainAll(elements.toSet())
    override fun iterator() = set.iterator()
}

