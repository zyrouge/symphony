package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import kotlin.math.max
import kotlin.math.roundToInt

data class ResponsiveGridData(val columnsCount: Int)

@Composable
fun ResponsiveGrid(tileSize: Float = 200f, content: LazyGridScope.(ResponsiveGridData) -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val cols =  max((this@BoxWithConstraints.maxWidth.value / tileSize).roundToInt(), 1)
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

@Composable
fun ResponsiveGridSizeAdjust(context: ViewContext, tileSize: Float, onTileSizeChange: (Float) -> Unit) {
    Row(
        modifier = Modifier.padding(
            MenuDefaults.DropdownMenuItemContentPadding.run {
                val horizontalPadding =
                    calculateLeftPadding(LayoutDirection.Ltr)
                PaddingValues(
                    start = horizontalPadding.div(2),
                    end = horizontalPadding.div(2),
                )
            },
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(context.symphony.t.GridSize)
            val width = LocalConfiguration.current.screenWidthDp.toFloat()
            val stops = width / 100f
            androidx.compose.material3.Slider(
                modifier = Modifier.fillMaxWidth(),
                value = stops - (width / tileSize) + 1,
                onValueChange = { onTileSizeChange(width / (stops - it + 1)) },
                valueRange = 1f..stops,
                steps = (stops - 2).roundToInt(),
            )
        }
    }
}