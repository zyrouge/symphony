package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun ResponsiveGrid(
    topBar: (@Composable () -> Unit)? = null,
    content: LazyGridScope.() -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val cols = (maxWidth.value / 200).roundToInt()
        val gridState = rememberLazyGridState()
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(cols),
            modifier = Modifier
                .padding(top = 4.dp)
                .drawScrollBar(gridState, cols)
        ) {
            topBar?.run {
                item(span = { GridItemSpan(cols) }) {
                    invoke()
                }
            }
            content()
        }
    }
}
