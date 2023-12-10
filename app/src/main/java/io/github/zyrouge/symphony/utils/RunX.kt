package io.github.zyrouge.symphony.utils

fun <T> runIfTrueOrDefault(value: Boolean, defaultValue: T, fn: () -> T) = when {
    value -> fn()
    else -> defaultValue
}

fun <T> T.runIfTrue(value: Boolean, fn: T.() -> T) = if (value) fn.invoke(this) else this
