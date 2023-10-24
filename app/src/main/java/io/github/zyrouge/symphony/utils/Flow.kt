package io.github.zyrouge.symphony.utils

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.transform

data class TimedEntity<T>(val value: T, val updatedAt: Long) {
    companion object {
        fun <T> now(value: T) = TimedEntity(
            value = value,
            updatedAt = System.currentTimeMillis(),
        )
    }
}

fun <T> timedFlow() = MutableStateFlow<TimedEntity<T>>()

fun <T> MutableSharedFlow<TimedEntity<T>>.emitTimed(value: T) {
    Log.i("SymLog", "${tryEmit(TimedEntity.now(value))}")
}

fun <T> Flow<TimedEntity<T>>.mapTimed(target: String): Flow<T> {
    var lastUpdatedAt: Long = System.currentTimeMillis()
    return transform { x ->
        Log.i("SymLog", "transform ${x.updatedAt} ${lastUpdatedAt}")
        if (x.value == target && x.updatedAt != lastUpdatedAt) {
            Log.i("SymLog", "yeah")
            lastUpdatedAt = x.updatedAt
            emit(x.value)
        }
    }
}
