package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.ScaffoldDialog
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTextInputTile(
    context: ViewContext,
    icon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    value: String,
    onReset: (() -> Unit)? = null,
    onChange: (String) -> Unit
) {
    var isOpen by remember { mutableStateOf(false) }

    Card(
        colors = SettingsTileDefaults.cardColors(),
        onClick = { isOpen = !isOpen }
    ) {
        ListItem(
            colors = SettingsTileDefaults.listItemColors(),
            leadingContent = { icon() },
            headlineContent = { title() },
            supportingContent = { Text(value) },
        )
    }

    if (isOpen) {
        var input by remember { mutableStateOf<String?>(null) }
        val modified by remember {
            derivedStateOf { !input.isNullOrBlank() && value != input }
        }

        ScaffoldDialog(
            onDismissRequest = {
                if (!modified) {
                    isOpen = false
                }
            },
            title = title,
            content = {
                Box(
                    modifier = Modifier
                        .padding(start = 20.dp, end = 20.dp, top = 16.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedBorderColor = DividerDefaults.color,
                        ),
                        value = input ?: value,
                        onValueChange = {
                            input = it
                        }
                    )
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
                        isOpen = false
                    }
                ) {
                    Text(context.symphony.t.Cancel)
                }
                TextButton(
                    enabled = modified,
                    onClick = {
                        input?.let { onChange(it) }
                        isOpen = false
                    }
                ) {
                    Text(context.symphony.t.Done)
                }
            },
        )
    }
}
