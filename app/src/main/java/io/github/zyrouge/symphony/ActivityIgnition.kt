package io.github.zyrouge.symphony

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ActivityIgnition : ViewModel() {
    private val readyFlow = MutableStateFlow(false)
    val ready = readyFlow.asStateFlow()

    internal fun emitReady() {
        if (readyFlow.value) {
            return
        }
        readyFlow.update {
            true
        }
    }
}
