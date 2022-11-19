package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.helpers.ViewContext

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
) {
    var showDropdown by remember { mutableStateOf(false) }
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
                    if (reverse) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    null,
                    modifier = iconModifier,
                )
            }
            Box {
                TextButton(
                    colors = textButtonStyle,
                    onClick = {
                        showDropdown = !showDropdown
                    }
                ) {
                    Text(sorts[sort]!!(context), style = currentTextStyle)
                }
                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = { showDropdown = false }
                ) {
                    sorts.map {
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
                                    onClick = {
                                        it.value(context)
                                    }
                                )
                            },
                            text = {
                                Text(it.value(context))
                            },
                            onClick = {
                                showDropdown = false
                                onSortChange(it.key)
                            }
                        )
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProvideTextStyle(currentTextStyle) {
                label()
            }
            Spacer(modifier = Modifier.width(4.dp))
            onShufflePlay?.let {
                IconButton(
                    colors = iconButtonStyle,
                    onClick = it,
                ) {
                    Icon(
                        Icons.Default.Shuffle,
                        null,
                        modifier = iconModifier,
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}
