package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import io.github.zyrouge.symphony.ui.components.settings.SettingsSliderDialog
import io.github.zyrouge.symphony.ui.helpers.ViewContext

data class ResponsiveGridData(val columnsCount: Int)

data class ResponsiveGridColumns(val horizontal: Int, val vertical: Int) {
    internal fun calculateColumns(height: Int, width: Int): Int {
        val columns = when {
            height > width -> vertical
            else -> horizontal
        }
        val columnWidth = width / columns
        return when {
            columnWidth < MIN_GRID_WIDTH -> width / MIN_GRID_WIDTH
            else -> columns
        }
    }

    companion object {
        const val MIN_GRID_WIDTH = 75
        const val DEFAULT_HORIZONTAL_COLUMNS = 4
        const val DEFAULT_VERTICAL_COLUMNS = 2
    }
}

@Composable
fun ResponsiveGrid(
    columns: ResponsiveGridColumns,
    content: LazyGridScope.(ResponsiveGridData) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val effectiveColumn = columns.calculateColumns(
            this@BoxWithConstraints.maxHeight.value.toInt(),
            this@BoxWithConstraints.maxWidth.value.toInt(),
        )
        val gridState = rememberLazyGridState()
        val responsiveGridData = ResponsiveGridData(columnsCount = effectiveColumn)

        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(effectiveColumn),
            modifier = Modifier.drawScrollBar(gridState, effectiveColumn)
        ) {
            content(responsiveGridData)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponsiveGridSizeAdjustBottomSheet(
    context: ViewContext,
    columns: ResponsiveGridColumns,
    onColumnsChange: (ResponsiveGridColumns) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val isVertical = LocalConfiguration.current.run { screenHeightDp > screenWidthDp }
    val maxWidth = LocalConfiguration.current.screenWidthDp
    val maxColumns = maxWidth / ResponsiveGridColumns.MIN_GRID_WIDTH
    val effectiveColumns by remember(isVertical, columns) {
        derivedStateOf {
            when {
                isVertical -> columns.vertical
                else -> columns.horizontal
            }
        }
    }

    SettingsSliderDialog(
        context,
        title = {
            Text(context.symphony.t.GridColumns)
        },
        initialValue = effectiveColumns.toFloat(),
        range = 1f..maxColumns.toFloat(),
        label = {
            Text(it.toInt().toString())
        },
        onValue = {
            it.toInt().toFloat()
        },
        onChange = {
            val nColumns = when {
                isVertical -> columns.copy(vertical = it.toInt())
                else -> columns.copy(horizontal = it.toInt())
            }
            onColumnsChange(nColumns)
        },
        onDismissRequest = onDismissRequest,
    )
}
