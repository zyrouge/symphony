package io.github.zyrouge.symphony.ui.helpers

import androidx.compose.runtime.snapshots.SnapshotStateList

fun <T> SnapshotStateList<T>.swap(to: Collection<T>) {
    clear()
    addAll(to)
}
