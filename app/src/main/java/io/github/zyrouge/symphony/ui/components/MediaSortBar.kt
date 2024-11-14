package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import kotlin.math.roundToInt

@Composable
fun <T : Enum<T>> MediaSortBar(
    context: ViewContext,
    reverse: Boolean,
    onReverseChange: (Boolean) -> Unit,
    sort: T,
    sorts: Map<T, (ViewContext) -> String>,
    onSortChange: (T) -> Unit,
    label: @Composable () -> Unit,
    onShufflePlay: (() -> Unit)? = null,
    gridSize: Float? = null,
    onGridSizeChange: ((Float) -> Unit)? = null,
) {
    var showSortDropdown by remember { mutableStateOf(false) }
    var showViewDropdown by remember { mutableStateOf(false) }
    val currentTextStyle = MaterialTheme.typography.bodySmall.run {
        copy(color = MaterialTheme.colorScheme.onSurface)
    }

    val iconButtonStyle = IconButtonDefaults.iconButtonColors(
        contentColor = currentTextStyle.color
    )
    val iconModifier = Modifier.size(20.dp)
    val textButtonStyle = ButtonDefaults.textButtonColors(
        contentColor = currentTextStyle.color
    )

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row {
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                colors = iconButtonStyle,
                onClick = { onReverseChange(!reverse) }
            ) {
                Icon(
                    if (reverse) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                    null,
                    modifier = iconModifier,
                )
            }
            Box {
                TextButton(
                    colors = textButtonStyle,
                    onClick = {
                        showSortDropdown = !showSortDropdown
                    }
                ) {
                    Text(sorts[sort]!!(context), style = currentTextStyle)
                }
                DropdownMenu(
                    expanded = showSortDropdown,
                    onDismissRequest = { showSortDropdown = false }
                ) {
                    sorts.map {
                        val onClick = {
                            showSortDropdown = false
                            onSortChange(it.key)
                        }

                        DropdownMenuItem(
                            contentPadding = MenuDefaults.DropdownMenuItemContentPadding.run {
                                val horizontalPadding =
                                    calculateLeftPadding(LayoutDirection.Ltr)
                                PaddingValues(
                                    start = horizontalPadding.div(2),
                                    end = horizontalPadding.times(4),
                                )
                            },
                            leadingIcon = {
                                RadioButton(
                                    selected = it.key == sort,
                                    onClick = onClick,
                                )
                            },
                            text = {
                                Text(it.value(context))
                            },
                            onClick = onClick,
                        )
                    }
                }
            }
            gridSize?.let {
                Box {
                    TextButton(
                        colors = textButtonStyle,
                        onClick = { showViewDropdown = !showViewDropdown },
                    ) {
                        Icon(
                            Icons.Filled.GridView,
                            null,
                            modifier = iconModifier,
                        )
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            null,
                            modifier = iconModifier,
                        )
                    }
                    DropdownMenu(
                        expanded = showViewDropdown,
                        onDismissRequest = { showViewDropdown = false }
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                MenuDefaults.DropdownMenuItemContentPadding.run {
                                    val horizontalPadding =
                                        calculateLeftPadding(LayoutDirection.Ltr)
                                    PaddingValues(
                                        start = horizontalPadding.div(2),
                                        end = horizontalPadding.div(2),
                                    )
                                },
                            )
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Text(text = "Grid size")
                                val width = LocalConfiguration.current.screenWidthDp.toFloat()
                                val stops = width / 100f
                                Slider(
                                    modifier = Modifier.width(192.dp),
                                    value = stops - (width / gridSize) + 1,
                                    onValueChange = { onGridSizeChange?.invoke(width / (stops - it + 1)) },
                                    valueRange = 1f..stops,
                                    steps = (stops - 2).roundToInt(),
                                )
                            }
                        }
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProvideTextStyle(currentTextStyle) {
                label()
            }
            onShufflePlay?.let {
                IconButton(
                    modifier = Modifier.padding(4.dp, 0.dp),
                    colors = iconButtonStyle,
                    onClick = it,
                ) {
                    Icon(
                        Icons.Filled.Shuffle,
                        null,
                        modifier = iconModifier,
                    )
                }
            }
            if (onShufflePlay == null) {
                Spacer(modifier = Modifier.width(20.dp))
            }
        }
    }
}

