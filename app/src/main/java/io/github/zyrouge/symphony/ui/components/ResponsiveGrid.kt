package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlin.math.roundToInt

data class ResponsiveGridData(val columnsCount: Int)

@Composable
fun ResponsiveGrid(content: LazyGridScope.(ResponsiveGridData) -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val cols = (maxWidth.value / 200).roundToInt()
        val gridState = rememberLazyGridState()
        val responsiveGridData = ResponsiveGridData(columnsCount = cols)

        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(cols),
            modifier = Modifier.drawScrollBar(gridState, cols)
        ) {
            content(responsiveGridData)
        }
    }
}
