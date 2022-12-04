package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
            headlineText = { title() },
            supportingText = { Text(value) },
        )
    }

    if (isOpen) {
        var input by remember { mutableStateOf<String?>(null) }
        val modified by remember {
            derivedStateOf { !input.isNullOrBlank() && value != input }
        }
        Dialog(
            onDismissRequest = {
                if (!modified) {
                    isOpen = false
                }
            }
        ) {
            Surface(modifier = Modifier.clip(RoundedCornerShape(8.dp))) {
                Column {
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
                    Box(modifier = Modifier.padding(20.dp, 0.dp)) {
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
                                isOpen = false
                            }
                        ) {
                            Text(context.symphony.t.cancel)
                        }
                        TextButton(
                            enabled = modified,
                            onClick = {
                                input?.let { onChange(it) }
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
