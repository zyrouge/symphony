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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import io.github.zyrouge.symphony.services.radio.PlaybackPosition
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.TimedContent
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.timer
import kotlin.math.max

@Composable
fun LyricsText(
    context: ViewContext,
    content: TimedContent,
    padding: PaddingValues,
    style: TextStyle = LocalTextStyle.current,
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    var playbackPosition by remember {
        mutableStateOf(context.symphony.radio.currentPlaybackPosition ?: PlaybackPosition.zero)
    }
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
    var playbackPositionTimer: Timer? = null

    LaunchedEffect(LocalContext.current) {
        coroutineScope.launch {
            playbackPositionTimer = timer(period = 50L) {
                playbackPosition = context.symphony.radio.currentPlaybackPosition
                    ?: PlaybackPosition.zero
                val isActiveIndexInvisible =
                    activeIndex > -1 && activeIndex < visibleRange.first && activeIndex > visibleRange.second
                if (isActiveIndexInvisible) return@timer
                val nActiveIndex = content.pairs.indexOfLast { x ->
                    x.first <= playbackPosition.played
                }
                if (nActiveIndex == -1 || activeIndex == nActiveIndex) return@timer
                activeIndex = nActiveIndex
                coroutineScope.launch {
                    val scrollIndex = calculateRelaxedScrollIndex(nActiveIndex, visibleRange)
                    scrollState.animateScrollToItem(scrollIndex)
                }
            }
        }
    }

    DisposableEffect(LocalContext.current) {
        onDispose {
            playbackPositionTimer?.cancel()
        }
    }

    LazyColumn(
        state = scrollState,
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = padding.calculateStartPadding(LocalLayoutDirection.current),
                end = padding.calculateEndPadding(LocalLayoutDirection.current),
            ),
    ) {
        item {
            Spacer(modifier = Modifier.height(padding.calculateTopPadding()))
        }
        itemsIndexed(content.pairs) { i, x ->
            val highlight = i < activeIndex
            val active = i == activeIndex

            Text(
                x.second,
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures { _ ->
                            if (content.pairs.getOrNull(activeIndex)?.first == x.first) {
                                return@detectTapGestures
                            }
                            context.symphony.radio.seek(x.first)
                            activeIndex = i
                            coroutineScope.launch {
                                val scrollIndex = calculateRelaxedScrollIndex(i, visibleRange)
                                scrollState.animateScrollToItem(scrollIndex)
                            }
                        }
                    },
                style = when {
                    active -> style.copy(fontWeight = FontWeight.Bold)
                    highlight -> style
                    else -> style.copy(color = style.color.copy(alpha = 0.5f))
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
