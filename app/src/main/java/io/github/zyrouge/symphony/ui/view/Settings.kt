package io.github.zyrouge.symphony.ui.view

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.zyrouge.symphony.services.AppMeta
import io.github.zyrouge.symphony.services.ThemeMode
import io.github.zyrouge.symphony.services.i18n.Translations
import io.github.zyrouge.symphony.ui.components.EventerEffect
import io.github.zyrouge.symphony.ui.components.TopAppBarMinimalTitle
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.AndroidXShorty

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
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    SideHeading(context.symphony.t.appearance)
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
                            Text(context.symphony.t.theme)
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
                    SwitchTile(
                        icon = {
                            Icon(Icons.Default.SkipNext, null)
                        },
                        title = {
                            Text(context.symphony.t.miniPlayerExtendedControls)
                        },
                        value = settings.miniPlayerExtendedControls,
                        onChange = { value ->
                            context.symphony.settings.setMiniPlayerExtendedControls(value)
                        }
                    )
                    SwitchTile(
                        icon = {
                            Icon(Icons.Default.GraphicEq, null)
                        },
                        title = {
                            Text(context.symphony.t.fadePlaybackInOut)
                        },
                        value = settings.fadePlayback,
                        onChange = { value ->
                            context.symphony.settings.setFadePlayback(value)
                        }
                    )
                    SwitchTile(
                        icon = {
                            Icon(Icons.Default.GraphicEq, null)
                        },
                        title = {
                            Text(context.symphony.t.fadePlaybackInOut)
                        },
                        value = settings.fadePlayback,
                        onChange = { value ->
                            context.symphony.settings.setFadePlayback(value)
                        }
                    )
                    SwitchTile(
                        icon = {
                            Icon(Icons.Default.CenterFocusWeak, null)
                        },
                        title = {
                            Text(context.symphony.t.requireAudioFocus)
                        },
                        value = settings.requireAudioFocus,
                        onChange = { value ->
                            context.symphony.settings.setRequireAudioFocus(value)
                        }
                    )
                    SwitchTile(
                        icon = {
                            Icon(Icons.Default.CenterFocusWeak, null)
                        },
                        title = {
                            Text(context.symphony.t.ignoreAudioFocusLoss)
                        },
                        value = settings.ignoreAudioFocusLoss,
                        onChange = { value ->
                            context.symphony.settings.setIgnoreAudioFocusLoss(value)
                        }
                    )
                    Divider()
                    SideHeading(context.symphony.t.groove)
                    val defaultSongsFilterPattern = ".*"
                    TextInputTile(
                        context,
                        icon = {
                            Icon(Icons.Default.FilterAlt, null)
                        },
                        title = {
                            Text(context.symphony.t.songsFilterPattern)
                        },
                        value = settings.songsFilterPattern ?: defaultSongsFilterPattern,
                        onReset = {
                            context.symphony.settings.setSongsFilterPattern(null)
                        },
                        onChange = { value ->
                            context.symphony.settings.setSongsFilterPattern(
                                when (value) {
                                    defaultSongsFilterPattern -> null
                                    else -> value
                                }
                            )
                        }
                    )
                    Divider()
                    SideHeading(context.symphony.t.about)
                    val isLatestVersion =
                        AppMeta.latestVersion?.let { it == AppMeta.version } ?: true
                    SimpleTile(
                        icon = {
                            Icon(Icons.Default.MusicNote, null)
                        },
                        title = {
                            Text("${AppMeta.appName} ${AppMeta.version}")
                        },
                        subtitle = when {
                            !isLatestVersion -> ({
                                Text(context.symphony.t.newVersionAvailableX(AppMeta.latestVersion!!))
                            })
                            else -> null
                        },
                        onClick = {
                            AndroidXShorty.startBrowserActivity(
                                context.activity,
                                when {
                                    isLatestVersion -> AppMeta.githubRepositoryUrl
                                    else -> AppMeta.githubLatestReleaseUrl
                                }
                            )
                        }
                    )
                    LinkTile(
                        context,
                        icon = {
                            Icon(Icons.Default.Favorite, null, tint = Color.Red)
                        },
                        title = {
                            Text(context.symphony.t.madeByX(AppMeta.author))
                        },
                        url = AppMeta.githubProfileUrl
                    )
                    LinkTile(
                        context,
                        icon = {
                            Icon(Icons.Default.Code, null)
                        },
                        title = {
                            Text(context.symphony.t.github)
                        },
                        url = AppMeta.githubRepositoryUrl
                    )
                    SwitchTile(
                        icon = {
                            Icon(Icons.Default.Update, null)
                        },
                        title = {
                            Text(context.symphony.t.checkForUpdates)
                        },
                        value = settings.checkForUpdates,
                        onChange = { value ->
                            context.symphony.settings.setCheckForUpdates(value)
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
private fun SimpleTile(
    icon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    subtitle: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    Card(
        colors = TileDefaults.cardColors(),
        onClick = onClick
    ) {
        ListItem(
            colors = TileDefaults.listItemColors(),
            leadingContent = { icon() },
            headlineText = { title() },
            supportingText = { subtitle?.let { it() } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LinkTile(
    context: ViewContext,
    icon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    url: String
) {
    Card(
        colors = TileDefaults.cardColors(),
        onClick = {
            AndroidXShorty.startBrowserActivity(context.activity, url)
        }
    ) {
        ListItem(
            colors = TileDefaults.listItemColors(),
            leadingContent = { icon() },
            headlineText = { title() },
            supportingText = { Text(url) }
        )
    }
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
        Dialog(onDismissRequest = { isOpen = false }) {
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

@Composable
private fun SideHeading(text: String) {
    Box(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextInputTile(
    context: ViewContext,
    icon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    value: String,
    onReset: (() -> Unit)? = null,
    onChange: (String) -> Unit
) {
    var isOpen by remember { mutableStateOf(false) }

    Card(
        colors = TileDefaults.cardColors(),
        onClick = { isOpen = !isOpen }
    ) {
        ListItem(
            colors = TileDefaults.listItemColors(),
            leadingContent = { icon() },
            headlineText = { title() },
            supportingText = { Text(value) },
        )
    }

    if (isOpen) {
        var input by remember { mutableStateOf<String?>(null) }

        Dialog(onDismissRequest = { isOpen = false }) {
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
                    Box(modifier = Modifier.padding(20.dp)) {
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
                                    onReset()
                                    isOpen = false
                                }
                            ) {
                                Text(context.symphony.t.reset)
                            }
                        }
                        TextButton(
                            enabled = input != null && input!!.isNotEmpty() && value != input,
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
