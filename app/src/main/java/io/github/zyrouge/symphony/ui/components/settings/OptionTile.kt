package io.github.zyrouge.symphony.ui.components.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.ScaffoldDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SettingsOptionTile(
    icon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    value: T,
    values: Map<T, String>,
    captions: Map<T, String>? = null,
    onChange: (T) -> Unit,
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
            supportingContent = { Text(values[value]!!) },
        )
    }

    if (isOpen) {
        ScaffoldDialog(
            onDismissRequest = {
                isOpen = false
            },
            title = title,
            content = {
                Column(
                    modifier = Modifier
                        .padding(0.dp, 8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    values.map { entry ->
                        val caption = captions?.get(entry.key)
                        val verticalSpace = when {
                            caption != null -> 4.dp
                            else -> 0.dp
                        }

                        Card(
                            colors = SettingsTileDefaults.cardColors(),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                onChange(entry.key)
                                isOpen = false
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp, verticalSpace),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = value == entry.key,
                                    onClick = {
                                        onChange(entry.key)
                                        isOpen = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(entry.value)
                                    caption?.let {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            caption,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = LocalContentColor.current.copy(alpha = 0.7f)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
        )
    }
}
