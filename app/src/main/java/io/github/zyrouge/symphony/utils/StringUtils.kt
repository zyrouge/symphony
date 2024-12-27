package io.github.zyrouge.symphony.utils

fun String.withCase(sensitive: Boolean) = if (!sensitive) lowercase() else this
