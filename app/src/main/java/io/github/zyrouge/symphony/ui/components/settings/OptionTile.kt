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
                                modifier = Modifier.padding(12.dp, 0.dp),
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
                                Text(entry.value)
                            }
                        }
                    }
                }
            },
        )
    }
}
