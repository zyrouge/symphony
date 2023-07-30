package io.github.zyrouge.symphony.utils

fun <T> runIfTrueOrDefault(value: Boolean, defaultValue: T, fn: () -> T) = when {
    value -> fn()
    else -> defaultValue
}
