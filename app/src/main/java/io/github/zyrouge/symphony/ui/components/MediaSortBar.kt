package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
) {
    var showDropdown by remember { mutableStateOf(false) }
    val currentTextStyle = MaterialTheme.typography.bodySmall.run {
        copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row {
            Box(
                modifier = Modifier.width(60.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = currentTextStyle.color
                    ),
                    onClick = {
                        onReverseChange(!reverse)
                    }
                ) {
                    Icon(
                        if (reverse) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        null,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Box {
                TextButton(
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = currentTextStyle.color
                    ),
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
                            colors = MenuDefaults.itemColors(
                                textColor = if (it.key == sort) currentTextStyle.color.copy(alpha = 1f)
                                else currentTextStyle.color
                            ),
                            text = {
                                Text(
                                    it.value(context),
                                    style = MaterialTheme.typography.bodySmall
                                )
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
        Box(modifier = Modifier.padding(16.dp, 0.dp)) {
            ProvideTextStyle(currentTextStyle) {
                label()
            }
        }
    }
}