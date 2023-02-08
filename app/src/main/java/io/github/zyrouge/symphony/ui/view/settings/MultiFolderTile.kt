package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.zyrouge.symphony.services.groove.*
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.navigateToFolder
import io.github.zyrouge.symphony.utils.swap
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
            headlineText = { title() },
            supportingText = {
                Text(context.symphony.t.XFolders(initialValues.size))
            },
        )
    }

    if (showDialog) {
        val values = remember {
            mutableStateListOf<String>().apply { swap(initialValues) }
        }
        var showPicker by remember { mutableStateOf(false) }

        // TODO: workaround for dialog resize bug
        //       https://issuetracker.google.com/issues/221643630
        key(values.size) {
            Dialog(
                onDismissRequest = {
                    onChange(values.toSet())
                    showDialog = false
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
                        Spacer(modifier = Modifier.height(8.dp))
                        values.mapIndexed { i, x ->
                            Card(
                                colors = SettingsTileDefaults.cardColors(),
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {},
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp, 0.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(x, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { values.removeAt(i) }) {
                                        Icon(Icons.Default.Delete, null)
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
                                    showPicker = true
                                }
                            ) {
                                Text(context.symphony.t.addFolder)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(
                                onClick = {
                                    showDialog = false
                                }
                            ) {
                                Text(context.symphony.t.cancel)
                            }
                            TextButton(
                                onClick = {
                                    onChange(values.toSet())
                                    showDialog = false
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

    Dialog(
        onDismissRequest = {
            when {
                currentFolder.parent != null -> currentFolder.parent?.let { currentFolder = it }
                else -> onSelect(null)
            }
        }
    ) {
        Surface(modifier = Modifier.clip(RoundedCornerShape(8.dp))) {
            BoxWithConstraints {
                val localDensity = LocalDensity.current
                var topBarHeight by remember { mutableStateOf(0.dp) }
                var bottomBarHeight by remember { mutableStateOf(0.dp) }
                val contentHeight by remember {
                    derivedStateOf {
                        maxHeight
                            .minus(topBarHeight)
                            .minus(bottomBarHeight)
                            .times(0.6f)
                    }
                }
                val currentPathScrollState = rememberScrollState()

                LaunchedEffect(LocalContext.current) {
                    snapshotFlow { currentPath }.collect {
                        currentPathScrollState.animateScrollTo(Int.MAX_VALUE)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned {
                            topBarHeight = with(localDensity) {
                                it.size.height.toDp()
                            }
                        }
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(20.dp, 0.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            context.symphony.t.pickFolder,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(currentPathScrollState)
                            .padding(12.dp, 0.dp),
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
                }
                Box(
                    modifier = Modifier
                        .height(contentHeight)
                        .padding(
                            top = topBarHeight,
                            bottom = bottomBarHeight,
                        )
                ) {
                    when {
                        sortedEntities.isEmpty() -> Box(
                            modifier = Modifier.height(contentHeight)
                        ) {
                            IconTextBody(
                                icon = { modifier ->
                                    Icon(
                                        Icons.Default.FolderOpen,
                                        null,
                                        modifier = modifier,
                                    )
                                },
                                content = {
                                    Text(context.symphony.t.damnThisIsSoEmpty)
                                }
                            )
                        }
                        else -> {
                            val lazyListState = rememberLazyListState()

                            LazyColumn(state = lazyListState) {
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
                                                        folder.countChildrenFolders()
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
                }
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(12.dp, 0.dp)
                        .onGloballyPositioned {
                            bottomBarHeight = with(localDensity) {
                                it.size.height.toDp()
                            }
                        }
                ) {
                    TextButton(onClick = { onSelect(null) }) {
                        Text(context.symphony.t.cancel)
                    }
                    TextButton(onClick = { onSelect(currentPath) }) {
                        Text(context.symphony.t.done)
                    }
                }
            }
        }
    }
}

private fun GrooveExplorer.Folder.countChildrenFolders() = children.values
    .count { it is GrooveExplorer.Folder }
