package io.github.zyrouge.symphony.utils.builtin

fun Float.toSafeFinite() = if (!isFinite()) 0f else this
