package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlin.math.roundToInt

data class ResponsiveGridData(val columnsCount: Int)

@Composable
fun ResponsiveGrid(
    topBar: (@Composable () -> Unit)? = null,
    content: LazyGridScope.(ResponsiveGridData) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val cols = (maxWidth.value / 200).roundToInt()
        val gridState = rememberLazyGridState()
        val responsiveGridData = ResponsiveGridData(columnsCount = cols)
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(cols),
            modifier = Modifier.drawScrollBar(gridState, cols)
        ) {
            topBar?.run {
                item(span = { GridItemSpan(cols) }) {
                    invoke()
                }
            }
            content(responsiveGridData)
        }
    }
}
