package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun SwipeActionWithPreview(
    modifier: Modifier = Modifier,
    onSwipe: () -> Unit,
    actionPreview: @Composable (progress: Float) -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val anchoredSwipeState = remember { AnchoredDraggableState(false) }

    val haptic = LocalHapticFeedback.current
    LaunchedEffect(anchoredSwipeState.settledValue) {
        if (anchoredSwipeState.settledValue) {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            onSwipe()
            anchoredSwipeState.animateTo(false)
        }
    }

    Box {
        Box(Modifier.matchParentSize().align(Alignment.TopStart)) {
            Box(Modifier.aspectRatio(1F), Alignment.Center) {
                actionPreview(min(anchoredSwipeState.progress(from = false, to = true) * 2.0F, 1.0F))
            }
        }
        Box(
            modifier =
                modifier
                    .onSizeChanged { size ->
                        anchoredSwipeState.updateAnchors(
                            DraggableAnchors {
                                false at 0F
                                true at size.height.toFloat()
                            },
                        )
                    }.anchoredDraggable(anchoredSwipeState, Orientation.Horizontal)
                    .offset { IntOffset(anchoredSwipeState.requireOffset().roundToInt(), 0) },
            content = content,
        )
    }
}
