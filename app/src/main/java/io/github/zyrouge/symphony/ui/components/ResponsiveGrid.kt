package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun ResponsiveGrid(
    count: Int,
    content: @Composable (BoxWithConstraintsScope.(Int) -> Unit)
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed((maxWidth.value / 200).roundToInt()),
            modifier = Modifier
                .padding(top = 4.dp)
        ) {
            items(count) { i ->
                content(i)
            }
        }
    }
}
