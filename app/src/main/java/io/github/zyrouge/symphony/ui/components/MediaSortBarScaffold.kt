package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity

@Composable
fun MediaSortBarScaffold(
    mediaSortBar: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val localDensity = LocalDensity.current
    var height by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.onGloballyPositioned {
                height = it.size.height
            }
        ) {
            mediaSortBar()
        }
        Box(
            modifier = Modifier
                .padding(top = with(localDensity) { height.toDp() })
        ) {
            content()
        }
    }
}
