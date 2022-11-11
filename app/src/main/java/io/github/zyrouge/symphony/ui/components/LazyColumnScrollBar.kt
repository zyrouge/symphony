package io.github.zyrouge.symphony.ui.components

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent

fun Modifier.drawScrollBar(state: LazyListState): Modifier = composed {
    val scrollPointerColor = MaterialTheme.colorScheme.primary
    val isLastItemVisible by remember {
        derivedStateOf {
            state.layoutInfo.visibleItemsInfo.lastOrNull()?.index == state.layoutInfo.totalItemsCount - 1
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
        animationSpec = tween(durationMillis = 50, easing = EaseInOut)
    )
    val showScrollPointerAnimated = animateFloatAsState(
        if (showScrollPointer) 1f else 0f,
        animationSpec = tween(durationMillis = 500)
    )
    drawWithContent {
        drawContent()
        scrollPointerOffsetY =
            if (isLastItemVisible) size.height - ContentDrawScopeScrollBarDefaults.scrollPointerHeight.toPx()
            else (size.height / state.layoutInfo.totalItemsCount) * state.firstVisibleItemIndex
        drawScrollBar(
            scrollPointerColor = scrollPointerColor,
            scrollPointerOffsetY = scrollPointerOffsetYAnimated.value,
            scrollPointerAlpha = showScrollPointerAnimated.value,
        )
    }
}
