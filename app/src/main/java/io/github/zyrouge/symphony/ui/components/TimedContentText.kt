package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import io.github.zyrouge.symphony.utils.TimedContent
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.max

data class TimedContentTextStyle(
    val highlighted: TextStyle,
    val active: TextStyle,
    val inactive: TextStyle,
) {
    companion object {
        @Composable
        fun defaultStyle() = LocalTextStyle.current.let {
            TimedContentTextStyle(
                highlighted = it,
                active = it.copy(fontWeight = FontWeight.Bold),
                inactive = it.copy(color = LocalContentColor.current.copy(alpha = 0.5f)),
            )
        }
    }
}

@Composable
fun TimedContentText(
    content: TimedContent,
    duration: Long,
    padding: PaddingValues,
    style: TimedContentTextStyle = TimedContentTextStyle.defaultStyle(),
    onSeek: (Int) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    val currentPosition by rememberUpdatedState(duration)
    val visibleRange by remember {
        derivedStateOf {
            val start = scrollState.firstVisibleItemIndex
            val end = start + scrollState.layoutInfo.visibleItemsInfo.size - 1
            start to end
        }
    }
    var activeIndex by remember {
        mutableStateOf(-1)
    }

    LaunchedEffect(LocalContext.current) {
        snapshotFlow { currentPosition }
            .distinctUntilChanged()
            .collect {
                if (!content.isSynced) return@collect
                val isActiveIndexInvisible = activeIndex > -1 && visibleRange.run {
                    activeIndex < first && activeIndex > second
                }
                if (isActiveIndexInvisible) return@collect
                val nActiveIndex = content.pairs.indexOfLast { x ->
                    x.first <= currentPosition
                }
                if (nActiveIndex == -1 || activeIndex == nActiveIndex) return@collect
                activeIndex = nActiveIndex
                if (scrollState.isScrollInProgress) return@collect
                coroutineScope.launch {
                    val scrollIndex = calculateRelaxedScrollIndex(nActiveIndex, visibleRange)
                    scrollState.animateScrollToItem(scrollIndex)
                }
            }
    }

    LazyColumn(
        state = scrollState,
        modifier = Modifier
            .padding(
                start = padding.calculateStartPadding(LocalLayoutDirection.current),
                end = padding.calculateEndPadding(LocalLayoutDirection.current),
            )
            .fillMaxSize(),
    ) {
        item {
            Spacer(modifier = Modifier.height(padding.calculateTopPadding()))
        }
        itemsIndexed(content.pairs) { i, x ->
            val highlight = !content.isSynced || i < activeIndex
            val active = i == activeIndex

            Text(
                x.second,
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures { _ ->
                            if (!content.isSynced) return@detectTapGestures
                            onSeek(i)
                            activeIndex = i
                            coroutineScope.launch {
                                val scrollIndex = calculateRelaxedScrollIndex(i, visibleRange)
                                scrollState.animateScrollToItem(scrollIndex)
                            }
                        }
                    },
                style = when {
                    active -> style.active
                    highlight -> style.highlighted
                    else -> style.inactive
                },
            )
        }
        item {
            Spacer(modifier = Modifier.height(padding.calculateBottomPadding()))
        }
    }
}

private fun calculateRelaxedScrollIndex(target: Int, range: Pair<Int, Int>): Int {
    val relaxLines = (range.second - range.first).floorDiv(3)
    return max(0, target - relaxLines)
}
