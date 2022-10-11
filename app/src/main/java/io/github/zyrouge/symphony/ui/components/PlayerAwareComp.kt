package io.github.zyrouge.symphony.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.PlayerEvent
import io.github.zyrouge.symphony.ui.view.helpers.ViewContext
import io.github.zyrouge.symphony.utils.EventUnsubscribeFn

@Composable
fun PlayerAwareComp(
    context: ViewContext,
    onEvent: (PlayerEvent) -> Unit,
    content: @Composable () -> Unit
) {
    var unsubscribe: EventUnsubscribeFn? = remember { null }

    LaunchedEffect(LocalLifecycleOwner.current) {
        unsubscribe = Symphony.player.events.subscribe {
            onEvent(it)
        }
    }

    DisposableEffect(LocalLifecycleOwner.current) {
        onDispose { unsubscribe?.invoke() }
    }

    content()
}
