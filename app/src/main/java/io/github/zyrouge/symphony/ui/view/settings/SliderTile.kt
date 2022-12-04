package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
    var value by remember { mutableStateOf(initialValue) }
    var ratio by remember { mutableStateOf(RangeUtils.calculateRatioFromValue(value, range)) }

    Card(
        colors = SettingsTileDefaults.cardColors(),
        onClick = {
            isOpen = !isOpen
        }
    ) {
        ListItem(
            colors = SettingsTileDefaults.listItemColors(),
            leadingContent = { icon() },
            headlineText = { title() },
            supportingText = { label(initialValue) },
        )
    }

    if (isOpen) {
        Dialog(onDismissRequest = { isOpen = false }) {
            Surface(modifier = Modifier.clip(RoundedCornerShape(8.dp))) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(20.dp, 0.dp)
                            .fillMaxWidth()
                    ) {
                        ProvideTextStyle(
                            value = MaterialTheme.typography.bodyLarge.copy(
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            title()
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
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
                                    var offsetX = 0f
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            offsetX = offset.x
                                        },
                                        onDrag = { pointer, offset ->
                                            pointer.consume()
                                            offsetX += offset.x
                                            val widthPx = maxWidth.toPx()
                                            val nRatio = (offsetX / widthPx).coerceIn(0f..1f)
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp, 0.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        onReset?.let {
                            TextButton(
                                onClick = {
                                    it()
                                    isOpen = false
                                }
                            ) {
                                Text(context.symphony.t.reset)
                            }
                        }
                        TextButton(
                            onClick = {
                                onChange(value)
                                isOpen = false
                            }
                        ) {
                            Text(context.symphony.t.done)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
