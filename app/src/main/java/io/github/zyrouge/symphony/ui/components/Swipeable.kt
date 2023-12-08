package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.swipeable(
    minimumDragAmount: Float = 50f,
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    onSwipeUp: (() -> Unit)? = null,
    onSwipeDown: (() -> Unit)? = null,
) = pointerInput(kotlin.Unit) {
    var offset = Offset.Zero
    detectDragGestures(
        onDrag = { pointer, dragAmount ->
            pointer.consume()
            offset += dragAmount
        },
        onDragEnd = {
            when {
                offset.x > minimumDragAmount -> onSwipeRight?.invoke()
                offset.x < -minimumDragAmount -> onSwipeLeft?.invoke()
                offset.y > minimumDragAmount -> onSwipeDown?.invoke()
                offset.y < -minimumDragAmount -> onSwipeUp?.invoke()
            }
            offset = Offset.Zero
        },
        onDragCancel = {
            offset = Offset.Zero
        }
    )
}
