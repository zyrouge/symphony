package io.github.zyrouge.symphony.ui.components

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import kotlin.math.floor

fun Modifier.drawScrollBar(state: LazyGridState, columns: Int): Modifier = composed {
    val scrollPointerColor = MaterialTheme.colorScheme.surfaceTint
    val isLastItemVisible by remember {
        derivedStateOf {
            state.layoutInfo.visibleItemsInfo.lastOrNull()?.index == state.layoutInfo.totalItemsCount - 1
        }
    }
    val rows by remember {
        derivedStateOf {
            floor(state.layoutInfo.totalItemsCount.toFloat() / columns)
        }
    }
    val showScrollPointer by remember {
        derivedStateOf {
            !(state.firstVisibleItemIndex == 0 && isLastItemVisible)
        }
    }
    var scrollPointerOffsetY by remember { mutableStateOf(0f) }
    val scrollPointerOffsetYAnimated = animateFloatAsState(
        scrollPointerOffsetY,
        animationSpec = tween(durationMillis = 150)
    )
    val showScrollPointerAnimated = animateFloatAsState(
        if (showScrollPointer) 1f else 0f,
        animationSpec = tween(durationMillis = 50, easing = EaseInOut)
    )
    drawWithContent {
        drawContent()
        val scrollBarHeight =
            size.height - ContentDrawScopeScrollBarDefaults.scrollPointerHeight.toPx()
        scrollPointerOffsetY =
            if (isLastItemVisible) scrollBarHeight
            else (scrollBarHeight / rows) * (state.firstVisibleItemIndex / columns)
        drawScrollBar(
            scrollPointerColor = scrollPointerColor,
            scrollPointerOffsetY = scrollPointerOffsetYAnimated.value,
            scrollPointerAlpha = showScrollPointerAnimated.value,
        )
    }
}
