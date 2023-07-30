package io.github.zyrouge.symphony.ui.components

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import io.github.zyrouge.symphony.utils.toSafeFinite
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
            floor(state.layoutInfo.totalItemsCount.toFloat() / columns).toSafeFinite()
        }
    }
    val showScrollPointer by remember {
        derivedStateOf {
            !(state.firstVisibleItemIndex == 0 && isLastItemVisible)
        }
    }
    var scrollPointerOffsetY by remember { mutableFloatStateOf(0f) }
    val scrollPointerOffsetYAnimated = animateFloatAsState(
        scrollPointerOffsetY,
        animationSpec = tween(durationMillis = 150),
        label = "c-lazy-grid-scroll-pointer-offset-y",
    )
    val showScrollPointerAnimated = animateFloatAsState(
        if (showScrollPointer) 1f else 0f,
        animationSpec = tween(durationMillis = 50, easing = EaseInOut),
        label = "c-lazy-grid-scroll-pointer-offset-y",
    )

    drawWithContent {
        drawContent()
        val scrollBarHeight =
            size.height - ContentDrawScopeScrollBarDefaults.scrollPointerHeight.toPx()
        val nScrollPointerOffsetY =
            if (isLastItemVisible) scrollBarHeight
            else (scrollBarHeight / rows) * (state.firstVisibleItemIndex / columns)
        scrollPointerOffsetY = nScrollPointerOffsetY.toSafeFinite()
        drawScrollBar(
            scrollPointerColor = scrollPointerColor,
            scrollPointerOffsetY = scrollPointerOffsetYAnimated.value,
            scrollPointerAlpha = showScrollPointerAnimated.value,
        )
    }
}
