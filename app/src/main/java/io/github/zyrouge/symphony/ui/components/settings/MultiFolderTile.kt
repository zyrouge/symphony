package io.github.zyrouge.symphony.ui.components.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.*
import io.github.zyrouge.symphony.ui.components.*
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.navigateToFolder
import java.util.*

private const val SettingsFolderContentType = "folder"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsMultiFolderTile(
    context: ViewContext,
    icon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    explorer: GrooveExplorer.Folder,
    initialValues: Set<String>,
    onChange: (Set<String>) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        colors = SettingsTileDefaults.cardColors(),
        onClick = {
            showDialog = !showDialog
        }
    ) {
        ListItem(
            colors = SettingsTileDefaults.listItemColors(),
            leadingContent = { icon() },
            headlineContent = { title() },
            supportingContent = {
                Text(context.symphony.t.XFolders(initialValues.size.toString()))
            },
        )
    }

    if (showDialog) {
        val values = remember { initialValues.toMutableStateList() }
        var showPicker by remember { mutableStateOf(false) }

        // TODO: workaround for dialog resize bug
        //       https://issuetracker.google.com/issues/221643630
        key(values.size) {
            ScaffoldDialog(
                onDismissRequest = {
                    onChange(values.toSet())
                    showDialog = false
                },
                title = title,
                contentHeight = ScaffoldDialogDefaults.PreferredMaxHeight,
                content = {
                    val lazyListState = rememberLazyListState()

                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .drawScrollBar(lazyListState),
                    ) {
                        itemsIndexed(values) { i, x ->
                            Card(
                                colors = SettingsTileDefaults.cardColors(),
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {},
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        start = 20.dp,
                                        end = 8.dp,
                                    ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(x, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { values.removeAt(i) }) {
                                        Icon(Icons.Default.Delete, null)
                                    }
                                }
                            }
                        }
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            showPicker = true
                        }
                    ) {
                        Text(context.symphony.t.AddFolder)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = {
                            showDialog = false
                        }
                    ) {
                        Text(context.symphony.t.Cancel)
                    }
                    TextButton(
                        onClick = {
                            onChange(values.toSet())
                            showDialog = false
                        }
                    ) {
                        Text(context.symphony.t.Done)
                    }
                }
            )
        }

        if (showPicker) {
            SettingsFolderTilePickerDialog(
                context,
                explorer = explorer,
                onSelect = {
                    if (it != null) {
                        val path = "/" + it.subList(1, it.size).joinToString("/")
                        if (!values.contains(path)) {
                            values.add(path)
                        }
                    }
                    showPicker = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsFolderTilePickerDialog(
    context: ViewContext,
    explorer: GrooveExplorer.Folder,
    onSelect: (List<String>?) -> Unit,
) {
    var currentFolder by remember { mutableStateOf(explorer) }
    val sortedEntities by remember {
        derivedStateOf {
            currentFolder.children.values.mapNotNull { entity ->
                when (entity) {
                    is GrooveExplorer.Folder -> entity
                    else -> null
                }
            }
        }
    }
    val currentPath by remember { derivedStateOf { currentFolder.fullPath } }
    val currentPathScrollState = rememberScrollState()

    LaunchedEffect(LocalContext.current) {
        snapshotFlow { currentPath }.collect {
            currentPathScrollState.animateScrollTo(Int.MAX_VALUE)
        }
    }

    ScaffoldDialog(
        onDismissRequest = {
            when {
                currentFolder.parent != null -> currentFolder.parent?.let { currentFolder = it }
                else -> onSelect(null)
            }
        },
        title = {
            Text(context.symphony.t.PickFolder)
        },
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(currentPathScrollState)
                    .padding(12.dp, 8.dp),
            ) {
                currentPath.mapIndexed { i, basename ->
                    Text(
                        basename,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                explorer
                                    .navigateToFolder(currentPath.subList(1, i + 1))
                                    ?.let { currentFolder = it }
                            }
                            .padding(8.dp, 4.dp),
                    )
                    if (i != currentPath.size - 1) {
                        Text(
                            "/",
                            modifier = Modifier
                                .padding(4.dp, 0.dp)
                                .alpha(0.3f)
                        )
                    }
                }
            }
        },
        contentHeight = ScaffoldDialogDefaults.PreferredMaxHeight,
        content = {
            when {
                sortedEntities.isEmpty() -> Box(
                    modifier = Modifier.fillMaxHeight()
                ) {
                    SubtleCaptionText(context.symphony.t.NoFoldersFound)
                }

                else -> {
                    val lazyListState = rememberLazyListState()

                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.drawScrollBar(lazyListState),
                    ) {
                        items(
                            sortedEntities,
                            key = { it.basename },
                            contentType = { SettingsFolderContentType }
                        ) { folder ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                onClick = {
                                    currentFolder = folder
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(20.dp, 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        Icons.Default.Folder,
                                        null,
                                        modifier = Modifier.size(32.dp),
                                    )
                                    Spacer(modifier = Modifier.width(20.dp))
                                    Column {
                                        Text(folder.basename)
                                        Text(
                                            context.symphony.t.XFolders(
                                                folder.countChildrenFolders().toString()
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        actions = {
            TextButton(onClick = { onSelect(null) }) {
                Text(context.symphony.t.Cancel)
            }
            TextButton(onClick = { onSelect(currentPath) }) {
                Text(context.symphony.t.Done)
            }
        },
    )
}

private fun GrooveExplorer.Folder.countChildrenFolders() = children.values
    .count { it is GrooveExplorer.Folder }
