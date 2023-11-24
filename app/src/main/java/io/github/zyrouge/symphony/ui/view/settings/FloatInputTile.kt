package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.ScaffoldDialog
import io.github.zyrouge.symphony.ui.helpers.ViewContext

private val floatInputRegex = Regex("""^\d+\.?\d{0,2}$""")

private fun isFloatInput(text: String) = floatInputRegex.matches(text)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsFloatInputTile(
    context: ViewContext,
    icon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    value: Float,
    presets: List<Float> = listOf(),
    labelText: ((Float) -> String) = { it.toString() },
    onReset: (() -> Unit)? = null,
    onChange: (Float) -> Unit,
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
            supportingContent = { Text(labelText(value)) },
        )
    }

    if (isOpen) {
        var input by remember {
            mutableStateOf(value.toString())
        }
        val inputValue by remember(input) {
            derivedStateOf { input.toFloatOrNull() }
        }
        val isError by remember(inputValue) {
            derivedStateOf { inputValue == null }
        }
        val modified by remember(inputValue, value) {
            derivedStateOf { inputValue != null && value != inputValue }
        }

        ScaffoldDialog(
            onDismissRequest = {
                if (!modified && !isError) {
                    isOpen = false
                }
            },
            title = title,
            content = {
                Column(
                    modifier = Modifier
                        .padding(start = 20.dp, end = 20.dp, top = 16.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            errorContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = DividerDefaults.color,
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = isError,
                        value = input,
                        onValueChange = {
                            if (it.isEmpty()) {
                                input = ""
                            } else if (isFloatInput(it)) {
                                input = it
                            }
                        }
                    )
                    if (presets.isNotEmpty()) {
                        Box(modifier = Modifier.height(12.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(
                                4.dp,
                                Alignment.CenterHorizontally,
                            ),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            presets.map { x ->
                                val active = inputValue == x
                                val xString = labelText(x)
                                val shape = RoundedCornerShape(4.dp)
                                val backgroundColor = when {
                                    active -> MaterialTheme.colorScheme.primaryContainer
                                    else -> Color.Transparent
                                }
                                val borderColor = when {
                                    active -> MaterialTheme.colorScheme.primaryContainer
                                    else -> DividerDefaults.color
                                }
                                val contentColor = when {
                                    active -> MaterialTheme.colorScheme.onPrimaryContainer
                                    else -> LocalContentColor.current
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(shape)
                                        .border(1.dp, borderColor, shape)
                                        .background(backgroundColor, shape)
                                        .clickable {
                                            input = x.toString()
                                        }
                                        .padding(5.dp, 2.dp),
                                ) {
                                    Text(
                                        xString,
                                        style = MaterialTheme.typography.labelMedium
                                            .copy(color = contentColor),
                                    )
                                }
                            }
                        }
                    }
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
                        inputValue?.let { onChange(it) }
                        isOpen = false
                    }
                ) {
                    Text(context.symphony.t.Done)
                }
            },
        )
    }
}
