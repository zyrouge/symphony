package io.github.zyrouge.symphony.ui.view.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.ScaffoldDialog
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.RangeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSliderTile(
    context: ViewContext,
    icon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    label: @Composable (Float) -> Unit,
    initialValue: Float,
    range: ClosedFloatingPointRange<Float>,
    onValue: (Float) -> Float = { it },
    onChange: (Float) -> Unit,
    onReset: (() -> Unit)? = null,
) {
    var isOpen by remember { mutableStateOf(false) }

    Card(
        colors = SettingsTileDefaults.cardColors(),
        onClick = {
            isOpen = !isOpen
        }
    ) {
        ListItem(
            colors = SettingsTileDefaults.listItemColors(),
            leadingContent = { icon() },
            headlineContent = { title() },
            supportingContent = { label(initialValue) },
        )
    }

    if (isOpen) {
        var value by remember { mutableFloatStateOf(initialValue) }
        var ratio by remember {
            mutableFloatStateOf(RangeUtils.calculateRatioFromValue(value, range))
        }
        var pointerOffsetX = 0f

        ScaffoldDialog(
            onDismissRequest = {
                isOpen = false
            },
            title = title,
            content = {
                Column(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
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
                                            value = onValue(nValue)
                                            ratio = RangeUtils.calculateRatioFromValue(value, range)
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
                                            value = onValue(nValue)
                                            ratio = RangeUtils.calculateRatioFromValue(value, range)
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
            },
            actions = {
                onReset?.let {
                    TextButton(
                        onClick = {
                            it()
                            isOpen = false
                        }
                    ) {
                        Text(context.symphony.t.Reset)
                    }
                }
                TextButton(
                    onClick = {
                        onChange(value)
                        isOpen = false
                    }
                ) {
                    Text(context.symphony.t.Done)
                }
            },
        )
    }
}
