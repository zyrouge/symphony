package io.github.zyrouge.symphony

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ActivityIgnition : ViewModel() {
    private val readyFlow = MutableStateFlow(false)
    val ready = readyFlow.asStateFlow()
    val isReady: Boolean
        get() = readyFlow.value

    fun toReady() {
        if (readyFlow.value) return
        viewModelScope.launch { readyFlow.emit(true) }
    }
}
