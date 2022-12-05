package io.github.zyrouge.symphony.ui.components

import androidx.activity.compose.BackHandler
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
import io.github.zyrouge.symphony.services.radio.Radio
import io.github.zyrouge.symphony.ui.helpers.ViewContext

private data class ExplorerResult(
    val folders: List<GrooveExplorer.Folder>,
    val files: Map<Song, GrooveExplorer.File>,
)

private const val FolderContentType = "folder"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerList(
    context: ViewContext,
    initialPath: List<String>?,
    explorer: GrooveExplorer.Folder,
    isLoading: Boolean = false,
    onPathChange: (List<String>) -> Unit,
) {
    var currentFolder by remember {
        mutableStateOf(
            initialPath
                ?.let { explorer.navigateToFolder(it.subList(1, it.size)) }
                ?: explorer
        )
    }
    var sortBy by remember {
        mutableStateOf(
            context.symphony.settings.getLastUsedFolderSortBy() ?: SongSortBy.FILENAME
        )
    }
    var sortReverse by remember {
        mutableStateOf(context.symphony.settings.getLastUsedFolderSortReverse())
    }
    val sortedEntities by remember {
        derivedStateOf {
            val folders = mutableListOf<GrooveExplorer.Folder>()
            val files = mutableMapOf<Song, GrooveExplorer.File>()
            currentFolder.children.values.forEach { entity ->
                when (entity) {
                    is GrooveExplorer.Folder -> folders.add(entity)
                    is GrooveExplorer.File -> {
                        context.symphony.groove.song
                            .getSongWithId(entity.data as Long)
                            ?.let { song -> files[song] = entity }
                    }
                }
            }
            ExplorerResult(
                folders = run {
                    val sorted = when (sortBy) {
                        SongSortBy.TITLE, SongSortBy.FILENAME -> folders.sortedBy { it.basename }
                        else -> folders
                    }
                    if (sortReverse) sorted.reversed() else sorted
                },
                files = SongRepository
                    .sort(files.keys.toList(), sortBy, sortReverse)
                    .associateWith { files[it]!! },
            )
        }
    }
    val currentPath by remember { derivedStateOf { currentFolder.fullPath } }

    LaunchedEffect(LocalContext.current) {
        snapshotFlow { currentPath }.collect { onPathChange(it) }
    }

    BackHandler(enabled = currentPath.size > 1) {
        currentFolder.parent?.let { currentFolder = it }
    }

    val lazyListState = rememberLazyListState()
    LazyColumn(
        state = lazyListState,
        modifier = Modifier.drawScrollBar(lazyListState)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
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
            Spacer(modifier = Modifier.height(12.dp))
            Divider()
        }
        item {
            MediaSortBar(
                context,
                reverse = sortReverse,
                onReverseChange = {
                    sortReverse = it
                    context.symphony.settings.setLastUsedSongsSortReverse(it)
                },
                sort = sortBy,
                sorts = SongSortBy.values().associateWith { x -> { x.label(it) } },
                onSortChange = {
                    sortBy = it
                    context.symphony.settings.setLastUsedSongsSortBy(it)
                },
                label = {
                    Text(
                        context.symphony.t.XFoldersYFiles(
                            sortedEntities.folders.size,
                            sortedEntities.files.size,
                        )
                    )
                },
                isLoading = isLoading,
                onShufflePlay = {
                    context.symphony.radio.shorty.playQueue(
                        sortedEntities.files.keys.toList(),
                        shuffle = true
                    )
                }
            )
        }
        items(
            sortedEntities.folders,
            key = { it.basename },
            contentType = { FolderContentType }
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
                            context.symphony.t.XItems(folder.children.size),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }
        itemsIndexed(
            sortedEntities.files.entries.toList(),
            key = { _, x -> x.key.id },
            contentType = { _, _ -> GrooveKinds.SONG }
        ) { i, entry ->
            SongCard(context, entry.key) {
                context.symphony.radio.shorty.playQueue(
                    sortedEntities.files.keys.toList(),
                    Radio.PlayOptions(index = i)
                )
            }
        }
    }
}

private fun SongSortBy.label(context: ViewContext) = when (this) {
    SongSortBy.TITLE -> context.symphony.t.title
    SongSortBy.ARTIST -> context.symphony.t.artist
    SongSortBy.ALBUM -> context.symphony.t.album
    SongSortBy.DURATION -> context.symphony.t.duration
    SongSortBy.DATE_ADDED -> context.symphony.t.dateAdded
    SongSortBy.DATE_MODIFIED -> context.symphony.t.lastModified
    SongSortBy.COMPOSER -> context.symphony.t.composer
    SongSortBy.ALBUM_ARTIST -> context.symphony.t.albumArtist
    SongSortBy.YEAR -> context.symphony.t.year
    SongSortBy.FILENAME -> context.symphony.t.filename
}

private fun GrooveExplorer.Folder.navigateToFolder(parts: List<String>): GrooveExplorer.Folder? {
    var folder: GrooveExplorer.Folder? = this
    parts.forEach { part ->
        folder = folder?.let {
            val child = it.children[part]
            if (child is GrooveExplorer.Folder) child else null
        }
    }
    return folder
}
