package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(cols),
            modifier = Modifier
                .padding(top = 4.dp)
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
