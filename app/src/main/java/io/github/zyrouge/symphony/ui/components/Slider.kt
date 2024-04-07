package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.utils.RangeUtils

@Composable
fun Slider(
    value: Float,
    onChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    label: @Composable (Float) -> Unit,
    modifier: Modifier,
) = Column(
    modifier = modifier
) {
    val ratio = RangeUtils.calculateRatioFromValue(value, range)
    var pointerOffsetX = 0f
    BoxWithConstraints(modifier = Modifier.padding(20.dp, 0.dp)) {
        val height = 12.dp
        val shape = RoundedCornerShape(height.div(2))

        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    shape,
                )
                .fillMaxWidth()
                .height(height)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            pointerOffsetX = offset.x
                            val widthPx = maxWidth.toPx()
                            val nRatio = (pointerOffsetX / widthPx).coerceIn(0f..1f)
                            val nValue =
                                RangeUtils.calculateValueFromRatio(nRatio, range)
                            onChange(nValue)
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            pointerOffsetX = offset.x
                        },
                        onHorizontalDrag = { pointer, dragAmount ->
                            pointer.consume()
                            pointerOffsetX += dragAmount
                            val widthPx = maxWidth.toPx()
                            val nRatio = (pointerOffsetX / widthPx).coerceIn(0f..1f)
                            val nValue =
                                RangeUtils.calculateValueFromRatio(nRatio, range)
                            onChange(nValue)
                        },
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primary,
                        shape,
                    )
                    .fillMaxWidth(ratio)
                    .height(height)
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    ProvideTextStyle(MaterialTheme.typography.labelMedium) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp, 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            label(range.start)
            label(value)
            label(range.endInclusive)
        }
    }
}