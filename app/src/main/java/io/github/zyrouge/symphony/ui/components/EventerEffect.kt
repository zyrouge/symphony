package io.github.zyrouge.symphony.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import io.github.zyrouge.symphony.utils.Eventer

@Composable
fun <T> EventerEffect(eventer: Eventer<T>, onEvent: (T) -> Unit) {
    DisposableEffect(LocalLifecycleOwner.current) {
        val unsubscribe = eventer.subscribe {
            onEvent(it)
        }

        onDispose { unsubscribe.invoke() }
    }
}
