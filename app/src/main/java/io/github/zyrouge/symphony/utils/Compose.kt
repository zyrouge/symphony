package io.github.zyrouge.symphony.utils

import android.annotation.SuppressLint
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier

class ModifierBuilder(var modifier: Modifier) {
    fun apply(fn: Modifier.() -> Modifier) {
        modifier = fn(modifier)
    }

    @SuppressLint("ModifierFactoryExtensionFunction")
    fun build() = modifier
}

fun Modifier.applyAll(fn: ModifierBuilder.() -> Unit): Modifier {
    val builder = ModifierBuilder(this)
    fn.invoke(builder)
    return builder.build()
}

fun <T> SnapshotStateList<T>.toImmutableDerivedState() = derivedStateOf { toList() }
