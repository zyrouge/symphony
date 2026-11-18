package io.github.zyrouge.symphony.utils

fun Float.toSafeFinite() = if (!isFinite()) 0f else this
