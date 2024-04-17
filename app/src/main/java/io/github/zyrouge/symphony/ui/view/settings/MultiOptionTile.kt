package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.ScaffoldDialog
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import java.util.Collections

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SettingsMultiOptionTile(
    context: ViewContext,
    icon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    note: (@Composable () -> Unit)? = null,
    value: Set<T>,
    values: Map<T, String>,
    satisfies: (Set<T>) -> Boolean = { true },
    onChange: (Set<T>) -> Unit,
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
            supportingContent = { Text(value.joinToString { values[it]!! }) },
        )
    }

    if (isOpen) {
        val nValue = remember { value.toMutableStateList() }
        val sortedValues by remember(nValue, values) {
            derivedStateOf {
                mutableSetOf<T>().apply {
                    addAll(nValue)
                    addAll(values.keys)
                }
            }
        }
        val satisfied by remember(nValue) {
            derivedStateOf { satisfies(nValue.toSet()) }
        }
        val modified by remember(nValue, value) {
            derivedStateOf { nValue.toSet() != value }
        }

        ScaffoldDialog(
            onDismissRequest = {
                if (!modified) {
                    isOpen = false
                }
            },
            title = title,
            topBar = {
                note?.let {
                    Box(
                        modifier = Modifier
                            .padding(start = 24.dp, end = 24.dp, top = 16.dp)
                            .alpha(0.7f)
                    ) {
                        ProvideTextStyle(MaterialTheme.typography.labelMedium) {
                            it()
                        }
                    }
                }
            },
            content = {
                Column(
                    modifier = Modifier
                        .padding(0.dp, 12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    sortedValues.mapIndexed { i, key ->
                        val selected = nValue.contains(key)
                        val toggleEntry: () -> Unit = {
                            if (nValue.contains(key)) {
                                nValue.remove(key)
                            } else {
                                nValue.add(key)
                            }
                        }
                        Card(
                            colors = SettingsTileDefaults.cardColors(),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = toggleEntry,
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp, 0.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selected,
                                        onCheckedChange = { toggleEntry() },
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(values[key]!!)
                                }
                                if (selected) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            enabled = i - 1 >= 0,
                                            onClick = {
                                                Collections.swap(nValue, i - 1, i)
                                            }
                                        ) {
                                            Icon(Icons.Filled.ArrowUpward, null)
                                        }
                                        IconButton(
                                            enabled = i + 1 < nValue.size,
                                            onClick = {
                                                Collections.swap(nValue, i + 1, i)
                                            }
                                        ) {
                                            Icon(Icons.Filled.ArrowDownward, null)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            removeActionsVerticalPadding = true,
            actions = {
                TextButton(
                    onClick = {
                        isOpen = false
                    }
                ) {
                    Text(context.symphony.t.Cancel)
                }
                TextButton(
                    enabled = modified && satisfied,
                    onClick = {
                        onChange(nValue.toSet())
                        isOpen = false
                    }
                ) {
                    Text(context.symphony.t.Done)
                }
            },
        )
    }
}
