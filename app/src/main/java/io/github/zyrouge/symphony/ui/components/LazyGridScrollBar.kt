package io.github.zyrouge.symphony.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import kotlin.math.floor

fun Modifier.drawScrollBar(state: LazyGridState, columns: Int): Modifier = composed {
    val scrollPointerColor = MaterialTheme.colorScheme.primary
    var scrollPointerOffsetY by remember { mutableStateOf(0f) }
    val scrollPointerOffsetYAnimated = animateFloatAsState(
        scrollPointerOffsetY,
        animationSpec = tween(durationMillis = 150)
    )
    var showScrollPointer by remember { mutableStateOf(false) }
    val showScrollPointerAnimated = animateFloatAsState(
        if (showScrollPointer) 1f else 0f,
        animationSpec = tween(durationMillis = 500)
    )
    drawWithContent {
        drawContent()
        val isLastItemVisible =
            state.layoutInfo.visibleItemsInfo.lastOrNull()?.index == state.layoutInfo.totalItemsCount - 1
        val rows = floor(state.layoutInfo.totalItemsCount.toFloat() / columns)
        showScrollPointer = !(state.firstVisibleItemIndex == 0 && isLastItemVisible)
        scrollPointerOffsetY =
            if (isLastItemVisible) size.height - ContentDrawScopeScrollBarDefaults.scrollPointerHeight.toPx()
            else (size.height / rows) * (state.firstVisibleItemIndex / columns)
        drawScrollBar(
            scrollPointerColor = scrollPointerColor,
            scrollPointerOffsetY = scrollPointerOffsetYAnimated.value,
            scrollPointerAlpha = showScrollPointerAnimated.value,
        )
    }
}
