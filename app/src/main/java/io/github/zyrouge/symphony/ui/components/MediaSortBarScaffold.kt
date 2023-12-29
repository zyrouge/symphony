package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity

@Composable
fun MediaSortBarScaffold(
    mediaSortBar: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    var height by remember { mutableIntStateOf(0) }

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
                .padding(top = with(density) { height.toDp() })
        ) {
            content()
        }
    }
}
