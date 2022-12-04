package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.strictEquals
import io.github.zyrouge.symphony.utils.swap
import java.util.*

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
            headlineText = { title() },
            supportingText = { Text(value.joinToString { values[it]!! }) },
        )
    }

    if (isOpen) {
        val nValue = remember {
            mutableStateListOf<T>().apply { swap(value) }
        }
        val sortedValues by remember {
            derivedStateOf {
                mutableSetOf<T>().apply {
                    addAll(nValue)
                    addAll(values.keys)
                }
            }
        }
        val satisfied by remember {
            derivedStateOf { satisfies(nValue.toSet()) }
        }
        val modified by remember {
            derivedStateOf { !nValue.strictEquals(value.toList()) }
        }
        Dialog(
            onDismissRequest = {
                if (!modified) {
                    isOpen = false
                }
            }
        ) {
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
                    note?.let {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(modifier = Modifier
                            .padding(24.dp, 0.dp)
                            .alpha(0.7f)) {
                            ProvideTextStyle(MaterialTheme.typography.labelMedium) {
                                it()
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
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
                                            Icon(Icons.Default.ArrowUpward, null)
                                        }
                                        IconButton(
                                            enabled = i + 1 < nValue.size,
                                            onClick = {
                                                Collections.swap(nValue, i + 1, i)
                                            }
                                        ) {
                                            Icon(Icons.Default.ArrowDownward, null)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp, 0.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(
                            onClick = {
                                isOpen = false
                            }
                        ) {
                            Text(context.symphony.t.cancel)
                        }
                        TextButton(
                            enabled = modified && satisfied,
                            onClick = {
                                onChange(nValue.toSet())
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
