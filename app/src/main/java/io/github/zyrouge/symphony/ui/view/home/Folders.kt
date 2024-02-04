package io.github.zyrouge.symphony.ui.view.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.GrooveExplorer
import io.github.zyrouge.symphony.services.groove.GrooveKinds
import io.github.zyrouge.symphony.services.groove.PathSortBy
import io.github.zyrouge.symphony.ui.components.AddToPlaylistDialog
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.components.MediaSortBar
import io.github.zyrouge.symphony.ui.components.MediaSortBarScaffold
import io.github.zyrouge.symphony.ui.components.ResponsiveGrid
import io.github.zyrouge.symphony.ui.components.SongList
import io.github.zyrouge.symphony.ui.components.SquareGrooveTile
import io.github.zyrouge.symphony.ui.components.label
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.ui.helpers.FadeTransition
import io.github.zyrouge.symphony.ui.helpers.SlideTransition
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.nowPlaying.defaultHorizontalPadding
import io.github.zyrouge.symphony.utils.wrapInViewContext
import java.util.Stack

@Composable
fun FoldersView(context: ViewContext) {
    val isUpdating by context.symphony.groove.song.isUpdating.collectAsState()
    val id by context.symphony.groove.song.id.collectAsState()
    val explorer = context.symphony.groove.song.explorer

    val folders = remember(id) {
        val entities = mutableMapOf<String, GrooveExplorer.Folder>()
        val stack = Stack<GrooveExplorer.Folder>()
        stack.add(explorer)
        while (stack.isNotEmpty()) {
            val current = stack.pop()
            if (current.isEmpty) continue
            var hasSongs = false
            current.children.values.forEach {
                when (it) {
                    is GrooveExplorer.Folder -> stack.push(it)
                    is GrooveExplorer.File -> {
                        hasSongs = true
                    }
                }
            }
            if (hasSongs) {
                entities[current.fullPath] = current
            }
        }
        entities.toMap()
    }
    var currentFolder by remember(id) {
        mutableStateOf<GrooveExplorer.Folder?>(null)
    }

    BackHandler(currentFolder != null) {
        currentFolder = null
    }

    LoaderScaffold(context, isLoading = isUpdating) {
        AnimatedContent(
            label = "folders-view-content",
            targetState = currentFolder,
            transitionSpec = {
                val enter = when {
                    targetState != null -> SlideTransition.slideUp.enterTransition()
                    else -> FadeTransition.enterTransition()
                }
                enter.togetherWith(FadeTransition.exitTransition())
            },
        ) { folder ->
            if (folder != null) {
                val songIds by remember(folder) {
                    derivedStateOf {
                        folder.children.values.mapNotNull {
                            when (it) {
                                is GrooveExplorer.File -> it.data as Long
                                else -> null
                            }
                        }
                    }
                }

                Column {
                    Column(
                        modifier = Modifier.padding(
                            start = defaultHorizontalPadding,
                            end = defaultHorizontalPadding,
                            top = 4.dp,
                            bottom = 12.dp,
                        ),
                    ) {
                        folder.parent?.let { parent ->
                            Text(
                                "${parent.fullPath}/",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = LocalContentColor.current.copy(alpha = 0.7f),
                                ),
                            )
                        }
                        Text(
                            folder.basename,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    HorizontalDivider()
                    SongList(context, songIds = songIds, songsCount = songIds.size)
                }
            } else {
                FoldersGrid(
                    context,
                    folders = folders,
                    onClick = {
                        currentFolder = it
                    }
                )
            }
        }
    }
}

@Composable
private fun FoldersGrid(
    context: ViewContext,
    folders: Map<String, GrooveExplorer.Folder>,
    onClick: (GrooveExplorer.Folder) -> Unit,
) {
    val sortBy by context.symphony.settings.lastUsedFoldersSortBy.collectAsState()
    val sortReverse by context.symphony.settings.lastUsedFoldersSortReverse.collectAsState()
    val sortedFolderNames by remember(folders, sortBy, sortReverse) {
        derivedStateOf {
            GrooveExplorer.sort(folders.keys.toList(), sortBy, sortReverse)
        }
    }

    MediaSortBarScaffold(
        mediaSortBar = {
            MediaSortBar(
                context,
                reverse = sortReverse,
                onReverseChange = {
                    context.symphony.settings.setLastUsedFoldersSortReverse(it)
                },
                sort = sortBy,
                sorts = PathSortBy.entries
                    .associateWith { x -> wrapInViewContext { x.label(context) } },
                onSortChange = {
                    context.symphony.settings.setLastUsedFoldersSortBy(it)
                },
                label = {
                    Text(context.symphony.t.XFolders(folders.size.toString()))
                },
            )
        },
        content = {
            when {
                sortedFolderNames.isEmpty() -> IconTextBody(
                    icon = { modifier ->
                        Icon(
                            Icons.Filled.FolderCopy,
                            null,
                            modifier = modifier,
                        )
                    },
                    content = { Text(context.symphony.t.DamnThisIsSoEmpty) }
                )

                else -> ResponsiveGrid {
                    itemsIndexed(
                        sortedFolderNames,
                        key = { i, x -> "$i-$x" },
                        contentType = { _, _ -> GrooveKinds.ARTIST }
                    ) { _, folderName ->
                        folders[folderName]?.let { folder ->
                            FolderTile(
                                context, folder = folder,
                                onClick = { onClick(folder) },
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun FolderTile(
    context: ViewContext,
    folder: GrooveExplorer.Folder,
    onClick: () -> Unit,
) {
    SquareGrooveTile(
        image = folder.createArtworkImageRequest(context).build(),
        options = { expanded, onDismissRequest ->
            var showAddToPlaylistDialog by remember { mutableStateOf(false) }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismissRequest
            ) {
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(Icons.AutoMirrored.Filled.PlaylistPlay, null)
                    },
                    text = {
                        Text(context.symphony.t.ShufflePlay)
                    },
                    onClick = {
                        onDismissRequest()
                        context.symphony.radio.shorty.playQueue(
                            folder.getSortedSongIds(context),
                            shuffle = true,
                        )
                    }
                )
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(Icons.AutoMirrored.Filled.PlaylistPlay, null)
                    },
                    text = {
                        Text(context.symphony.t.PlayNext)
                    },
                    onClick = {
                        onDismissRequest()
                        context.symphony.radio.queue.add(
                            folder.getSortedSongIds(context),
                            context.symphony.radio.queue.currentSongIndex + 1
                        )
                    }
                )
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null)
                    },
                    text = {
                        Text(context.symphony.t.AddToPlaylist)
                    },
                    onClick = {
                        onDismissRequest()
                        showAddToPlaylistDialog = true
                    }
                )
            }

            if (showAddToPlaylistDialog) {
                AddToPlaylistDialog(
                    context,
                    songIds = folder.getSortedSongIds(context),
                    onDismissRequest = {
                        showAddToPlaylistDialog = false
                    }
                )
            }
        },
        content = {
            Text(
                folder.basename,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        onPlay = {
            val sortedSongIds = folder.getSortedSongIds(context)
            context.symphony.radio.shorty.playQueue(sortedSongIds)
        },
        onClick = onClick,
    )
}

private fun GrooveExplorer.Folder.createArtworkImageRequest(context: ViewContext) = children.values
    .find { it is GrooveExplorer.File }
    ?.let {
        val songId = (it as GrooveExplorer.File).data as Long
        context.symphony.groove.song.createArtworkImageRequest(songId)
    }
    ?: Assets.createPlaceholderImageRequest(context.symphony)

private fun GrooveExplorer.Folder.getSortedSongIds(context: ViewContext): List<Long> {
    val songIds = children.values.mapNotNull {
        when (it) {
            is GrooveExplorer.File -> it.data as Long
            else -> null
        }
    }
    return context.symphony.groove.song.sort(
        songIds,
        context.symphony.settings.getLastUsedSongsSortBy(),
        context.symphony.settings.getLastUsedSongsSortReverse(),
    )
}
