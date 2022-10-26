package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.zyrouge.symphony.services.ThemeMode
import io.github.zyrouge.symphony.services.i18n.Translations
import io.github.zyrouge.symphony.ui.components.EventerEffect
import io.github.zyrouge.symphony.ui.components.TopAppBarMinimalTitle
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(context: ViewContext) {
    var settings by remember { mutableStateOf(context.symphony.settings.getSettings()) }

    EventerEffect(context.symphony.settings.onChange) {
        settings = context.symphony.settings.getSettings()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    TopAppBarMinimalTitle {
                        Text(context.symphony.t.settings)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            context.navController.popBackStack()
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        },
        content = { contentPadding ->
            Box(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
            ) {
                Column {
                    MultiOptionTile(
                        icon = {
                            Icon(Icons.Default.Language, null)
                        },
                        title = {
                            Text(context.symphony.t.language_)
                        },
                        value = settings.language ?: context.symphony.t.language,
                        values = Translations.all.associate {
                            it.language to it.language
                        },
                        onChange = { value ->
                            context.symphony.settings.setLanguage(value)
                        }
                    )
                    MultiOptionTile(
                        icon = {
                            Icon(Icons.Default.Palette, null)
                        },
                        title = {
                            Text(context.symphony.t.language_)
                        },
                        value = settings.themeMode,
                        values = mapOf(
                            ThemeMode.SYSTEM to context.symphony.t.system,
                            ThemeMode.LIGHT to context.symphony.t.light,
                            ThemeMode.DARK to context.symphony.t.dark,
                            ThemeMode.BLACK to context.symphony.t.black,
                        ),
                        onChange = { value ->
                            context.symphony.settings.setThemeMode(value)
                        }
                    )
                    SwitchTile(
                        icon = {
                            Icon(Icons.Default.Face, null)
                        },
                        title = {
                            Text(context.symphony.t.materialYou)
                        },
                        value = settings.useMaterialYou,
                        onChange = { value ->
                            context.symphony.settings.setUseMaterialYou(value)
                        }
                    )
                }
            }
        }
    )
}

private object TileDefaults {
    @Composable
    fun cardColors() = CardDefaults.cardColors(containerColor = Color.Transparent)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun listItemColors() = ListItemDefaults.colors(containerColor = Color.Transparent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwitchTile(
    icon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    value: Boolean,
    onChange: (Boolean) -> Unit
) {
    Card(
        colors = TileDefaults.cardColors(),
        onClick = {
            onChange(!value)
        }
    ) {
        ListItem(
            colors = TileDefaults.listItemColors(),
            leadingContent = { icon() },
            headlineText = { title() },
            trailingContent = {
                Switch(
                    checked = value,
                    onCheckedChange = {
                        onChange(!value)
                    }
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> MultiOptionTile(
    icon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    value: T,
    values: Map<T, String>,
    onChange: (T) -> Unit
) {
    var isOpen by remember { mutableStateOf(false) }

    Card(
        colors = TileDefaults.cardColors(),
        onClick = {
            isOpen = !isOpen
        }
    ) {
        ListItem(
            colors = TileDefaults.listItemColors(),
            leadingContent = { icon() },
            headlineText = { title() },
            supportingText = { Text(values[value]!!) },
        )
    }

    if (isOpen) {
        Dialog(
            onDismissRequest = {
                isOpen = false
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
                    Spacer(modifier = Modifier.height(8.dp))
                    values.map { entry ->
                        Card(
                            colors = TileDefaults.cardColors(),
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
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
