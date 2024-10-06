package io.github.zyrouge.symphony.utils

import java.util.concurrent.ConcurrentHashMap

fun Set<String>.joinToStringIfNotEmpty() = if (isNotEmpty()) joinToString() else null

typealias ConcurrentSet<T> = ConcurrentHashMap.KeySetView<T, Boolean>

fun <T> concurrentSetOf(vararg elements: T): ConcurrentSet<T> =
    ConcurrentHashMap.newKeySet<T>().apply { addAll(elements) }

fun <T> concurrentSetOf(elements: Collection<T>): ConcurrentSet<T> =
    ConcurrentHashMap.newKeySet<T>().apply { addAll(elements) }
