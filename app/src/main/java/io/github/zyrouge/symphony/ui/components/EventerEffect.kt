package io.github.zyrouge.symphony.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import io.github.zyrouge.symphony.utils.EventUnsubscribeFn
import io.github.zyrouge.symphony.utils.Eventer

@Composable
fun <T> EventerEffect(eventer: Eventer<T>, onEvent: (T) -> Unit) {
    var unsubscribe: EventUnsubscribeFn? = remember { null }

    LaunchedEffect(LocalLifecycleOwner.current) {
        unsubscribe = eventer.subscribe {
            onEvent(it)
        }
    }

    DisposableEffect(LocalLifecycleOwner.current) {
        onDispose { unsubscribe?.invoke() }
    }
}
