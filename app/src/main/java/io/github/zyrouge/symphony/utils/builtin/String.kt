package io.github.zyrouge.symphony.utils.builtin

fun String.withCase(sensitive: Boolean) = if (!sensitive) lowercase() else this

fun String.repeatJoin(count: Int, separator: String) = when (count) {
    0 -> ""
    1 -> this
    else -> (this + separator).repeat(count - 1) + this
}

// SQL Query Placeholders
fun sqlqph(count: Int) = "?".repeatJoin(count, ", ")
