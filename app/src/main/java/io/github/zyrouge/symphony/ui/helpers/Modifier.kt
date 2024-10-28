package io.github.zyrouge.symphony.ui.helpers

import androidx.compose.ui.Modifier

fun Modifier.applyIf(value: Boolean, fn: Modifier.() -> Modifier) = when {
    value -> fn()
    else -> this
}
