package io.github.zyrouge.symphony.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.GrooveExplorer
import io.github.zyrouge.symphony.services.groove.GrooveKinds
import io.github.zyrouge.symphony.services.groove.SongSortBy
import io.github.zyrouge.symphony.services.radio.Radio
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.navigateToFolder
import io.github.zyrouge.symphony.utils.wrapInViewContext

private data class SongExplorerResult(
    val folders: List<GrooveExplorer.Folder>,
    val files: Map<Long, GrooveExplorer.File>,
)

private const val SongFolderContentType = "folder"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongExplorerList(
    context: ViewContext,
    key: Any?,
    initialPath: List<String>?,
    explorer: GrooveExplorer.Folder,
    onPathChange: (List<String>) -> Unit,
) {
    var currentFolder by remember(key) {
        mutableStateOf(
            initialPath
                ?.let { explorer.navigateToFolder(it.subList(1, it.size)) }
                ?: explorer
        )
    }
    val sortBy by context.symphony.settings.lastUsedBrowserSortBy.collectAsState()
    val sortReverse by context.symphony.settings.lastUsedBrowserSortReverse.collectAsState()
    val sortedEntities by remember(key, currentFolder) {
        derivedStateOf {
            val categorized = currentFolder.categorizedChildren()

            SongExplorerResult(
                folders = run {
                    val sorted = when (sortBy) {
                        SongSortBy.TITLE,
                        SongSortBy.FILENAME,
                        -> categorized.folders.sortedBy { it.basename }

                        else -> categorized.folders
                    }
                    if (sortReverse) sorted.reversed() else sorted
                },
                files = context.symphony.groove.song
                    .sort(categorized.files.keys.toList(), sortBy, sortReverse)
                    .associateWith { categorized.files[it]!! },
            )
        }
    }
    val currentPath by remember(currentFolder) {
        derivedStateOf { currentFolder.pathParts }
    }
    val currentPathScrollState = rememberScrollState()

    LaunchedEffect(LocalContext.current) {
        snapshotFlow { currentPath }.collect { path ->
            onPathChange(path)
            currentPathScrollState.animateScrollTo(Int.MAX_VALUE)
        }
    }

    BackHandler(enabled = currentPath.size > 1) {
        currentFolder.parent?.let { currentFolder = it }
    }

    MediaSortBarScaffold(
        mediaSortBar = {
            Column(modifier = Modifier.wrapContentHeight()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
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
                                    .alpha(0.3f),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                MediaSortBar(
                    context,
                    reverse = sortReverse,
                    onReverseChange = {
                        context.symphony.settings.setLastUsedBrowserSortReverse(it)
                    },
                    sort = sortBy,
                    sorts = SongSortBy.entries
                        .associateWith { x -> wrapInViewContext { x.label(it) } },
                    onSortChange = {
                        context.symphony.settings.setLastUsedBrowserSortBy(it)
                    },
                    label = {
                        Text(
                            context.symphony.t.XFoldersYFiles(
                                sortedEntities.folders.size.toString(),
                                sortedEntities.files.size.toString(),
                            )
                        )
                    },
                    onShufflePlay = {
                        context.symphony.radio.shorty.playQueue(
                            sortedEntities.files.keys.toList(),
                            shuffle = true
                        )
                    }
                )
            }
        },
        content = {
            when {
                currentFolder.isEmpty -> IconTextBody(
                    icon = { modifier ->
                        Icon(
                            Icons.Filled.MusicNote,
                            null,
                            modifier = modifier,
                        )
                    },
                    content = { Text(context.symphony.t.DamnThisIsSoEmpty) }
                )

                else -> {
                    val lazyListState = rememberLazyListState()

                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.drawScrollBar(lazyListState),
                    ) {
                        items(
                            sortedEntities.folders,
                            key = { it.fullPath },
                            contentType = { SongFolderContentType }
                        ) { folder ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                onClick = {
                                    currentFolder = folder
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        start = 20.dp,
                                        end = 4.dp,
                                        top = 12.dp,
                                        bottom = 12.dp,
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        Icons.Filled.Folder,
                                        null,
                                        modifier = Modifier.size(32.dp),
                                    )
                                    Spacer(modifier = Modifier.width(20.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            folder.basename,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            context.symphony.t.XItems(folder.children.size.toString()),
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    }

                                    var showOptionsMenu by remember { mutableStateOf(false) }
                                    IconButton(
                                        onClick = {
                                            showOptionsMenu = !showOptionsMenu
                                        }
                                    ) {
                                        Icon(Icons.Filled.MoreVert, null)
                                        GenericSongListDropdown(
                                            context,
                                            songIds = folder.childrenAsSongIds(),
                                            expanded = showOptionsMenu,
                                            onDismissRequest = {
                                                showOptionsMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        itemsIndexed(
                            sortedEntities.files.entries.toList(),
                            key = { i, x -> "$i-${x.key}" },
                            contentType = { _, _ -> GrooveKinds.SONG }
                        ) { i, entry ->
                            context.symphony.groove.song.get(entry.key)?.let { song ->
                                SongCard(context, song) {
                                    context.symphony.radio.shorty.playQueue(
                                        sortedEntities.files.keys.toList(),
                                        Radio.PlayOptions(index = i)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        })
}

private data class GrooveExplorerCategorizedData(
    val folders: List<GrooveExplorer.Folder>,
    val files: Map<Long, GrooveExplorer.File>,
)

private fun GrooveExplorer.Folder.categorizedChildren(): GrooveExplorerCategorizedData {
    val folders = mutableListOf<GrooveExplorer.Folder>()
    val files = mutableMapOf<Long, GrooveExplorer.File>()
    children.values.forEach { entity ->
        when (entity) {
            is GrooveExplorer.Folder -> folders.add(entity)
            is GrooveExplorer.File -> {
                files[entity.data as Long] = entity
            }
        }
    }
    return GrooveExplorerCategorizedData(folders = folders, files = files)
}

private fun GrooveExplorer.Folder.childrenAsSongIds() = children.values.mapNotNull { entity ->
    when (entity) {
        is GrooveExplorer.File -> entity.data as Long
        else -> null
    }
}
