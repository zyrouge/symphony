package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.zyrouge.symphony.services.groove.repositories.SongRepository
import io.github.zyrouge.symphony.services.radio.Radio
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.SimplePath
import io.github.zyrouge.symphony.utils.StringSorter

@Composable
fun SongTreeList(
    context: ViewContext,
    songIds: List<String>,
    songsCount: Int? = null,
    initialDisabled: List<String>,
    onDisable: ((List<String>) -> Unit),
) {
    val tree by remember(songIds) {
        derivedStateOf { createLinearTree(context, songIds) }
    }
    val disabled = remember {
        mutableStateListOf<String>().apply {
            addAll(initialDisabled)
        }
    }
    val pathsSortBy by context.symphony.settings.lastUsedTreePathSortBy.flow.collectAsStateWithLifecycle()
    val pathsSortReverse by context.symphony.settings.lastUsedTreePathSortReverse.flow.collectAsStateWithLifecycle()
    val songsSortBy by context.symphony.settings.lastUsedSongsSortBy.flow.collectAsStateWithLifecycle()
    val songsSortReverse by context.symphony.settings.lastUsedSongsSortReverse.flow.collectAsStateWithLifecycle()
    val sortedTree by remember(tree, pathsSortBy, pathsSortReverse, songsSortBy, songsSortReverse) {
        derivedStateOf {
            val pairs = StringSorter.sort(tree.keys.toList(), pathsSortBy, pathsSortReverse)
                .map {
                    it to context.symphony.groove.song.sort(
                        tree[it]!!,
                        songsSortBy,
                        songsSortReverse
                    )
                }
            mapOf(*pairs.toTypedArray())
        }
    }
    val sortedSongIds by remember(sortedTree) {
        derivedStateOf { sortedTree.values.flatten() }
    }

    MediaSortBarScaffold(
        mediaSortBar = {
            SongTreeListMediaSortBar(
                context,
                songsCount = songsCount ?: songIds.size,
                pathsSortBy = pathsSortBy,
                pathsSortReverse = pathsSortReverse,
                songsSortBy = songsSortBy,
                songsSortReverse = songsSortReverse,
                setPathsSortBy = {
                    context.symphony.settings.lastUsedTreePathSortBy.setValue(it)
                },
                setPathsSortReverse = {
                    context.symphony.settings.lastUsedTreePathSortReverse.setValue(it)
                },
                setSongsSortBy = {
                    context.symphony.settings.lastUsedSongsSortBy.setValue(it)
                },
                setSongsSortReverse = {
                    context.symphony.settings.lastUsedSongsSortReverse.setValue(it)
                },
            )
        },
        content = {
            when {
                songIds.isEmpty() -> IconTextBody(
                    icon = { modifier ->
                        Icon(
                            Icons.Filled.MusicNote,
                            null,
                            modifier = modifier,
                        )
                    },
                    content = { Text(context.symphony.t.DamnThisIsSoEmpty) }
                )

                else -> SongTreeListContent(
                    context,
                    tree = sortedTree,
                    songIds = sortedSongIds,
                    disabled = disabled,
                    togglePath = { dirname ->
                        when {
                            disabled.contains(dirname) -> disabled.remove(dirname)
                            else -> disabled.add(dirname)
                        }
                        onDisable(disabled.toList())
                    }
                )
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongTreeListContent(
    context: ViewContext,
    tree: Map<String, List<String>>,
    songIds: List<String>,
    disabled: List<String>,
    togglePath: (String) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val queue by context.symphony.radio.observatory.queue.collectAsStateWithLifecycle()
    val queueIndex by context.symphony.radio.observatory.queueIndex.collectAsStateWithLifecycle()
    val currentPlayingSongId by remember(queue, queueIndex) {
        derivedStateOf { queue.getOrNull(queueIndex) }
    }
    val favoriteIds by context.symphony.groove.playlist.favorites.collectAsStateWithLifecycle()

    LazyColumn(
        state = lazyListState,
        modifier = Modifier.drawScrollBar(lazyListState),
    ) {
        tree.forEach { (dirname, childSongIds) ->
            val show = !disabled.contains(dirname)
            val sepPadding = if (show) 4.dp else 0.dp

            stickyHeader {
                Box(modifier = Modifier.padding(bottom = sepPadding)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                            .clickable { togglePath(dirname) }
                            .padding(
                                start = 12.dp,
                                end = 8.dp,
                                top = 4.dp,
                                bottom = 4.dp
                            )
                    ) {
                        Icon(
                            when {
                                show -> Icons.Filled.ExpandMore
                                else -> Icons.Filled.ChevronRight
                            },
                            null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(dirname, style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.weight(1f))

                        var showOptionsMenu by remember { mutableStateOf(false) }
                        SongTreeListSongCardIconButton(
                            icon = { modifier ->
                                Icon(
                                    Icons.Filled.MoreVert,
                                    null,
                                    modifier = modifier,
                                )
                                GenericSongListDropdown(
                                    context,
                                    songIds = childSongIds,
                                    expanded = showOptionsMenu,
                                    onDismissRequest = {
                                        showOptionsMenu = false
                                    }
                                )
                            },
                            onClick = {
                                showOptionsMenu = !showOptionsMenu
                            }
                        )
                    }
                }
            }

            if (show) {
                items(childSongIds) { songId ->
                    context.symphony.groove.song.get(songId)?.let { song ->
                        val isCurrentPlaying by remember(song, currentPlayingSongId) {
                            derivedStateOf { song.id == currentPlayingSongId }
                        }
                        val isFavorite by remember(favoriteIds, song) {
                            derivedStateOf { favoriteIds.contains(song.id) }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(5.dp))
                                .clickable {
                                    context.symphony.radio.shorty.playQueue(
                                        songIds,
                                        Radio.PlayOptions(index = songIds.indexOf(song.id))
                                    )
                                }
                                .padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp)
                        ) {
                            AsyncImage(
                                song.createArtworkImageRequest(context.symphony)
                                    .build(),
                                null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    song.title,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = when {
                                            isCurrentPlaying -> MaterialTheme.colorScheme.primary
                                            else -> LocalTextStyle.current.color
                                        }
                                    ),
                                )
                                if (song.artists.isNotEmpty()) {
                                    Text(
                                        song.artists.joinToString(),
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                            }
                            Row {
                                if (isFavorite) {
                                    SongTreeListSongCardIconButton(
                                        icon = { modifier ->
                                            Icon(
                                                Icons.Filled.Favorite,
                                                null,
                                                modifier = modifier,
                                                tint = MaterialTheme.colorScheme.primary,
                                            )
                                        },
                                        onClick = {
                                            context.symphony.groove.playlist.unfavorite(song.id)
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))

                                var showOptionsMenu by remember { mutableStateOf(false) }
                                SongTreeListSongCardIconButton(
                                    icon = { modifier ->
                                        Icon(
                                            Icons.Filled.MoreVert,
                                            null,
                                            modifier = modifier,
                                        )
                                        SongDropdownMenu(
                                            context,
                                            song,
                                            isFavorite = isFavorite,
                                            expanded = showOptionsMenu,
                                            onDismissRequest = {
                                                showOptionsMenu = false
                                            }
                                        )
                                    },
                                    onClick = {
                                        showOptionsMenu = !showOptionsMenu
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(top = sepPadding))
            }
        }
    }
}

@Composable
fun SongTreeListSongCardIconButton(
    icon: @Composable (Modifier) -> Unit,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .clickable { onClick() }
    ) {
        icon(
            Modifier
                .size(20.dp)
                .padding(start = 5.dp, top = 5.dp),
        )
    }
}

@Composable
private fun SongTreeListMediaSortBar(
    context: ViewContext,
    songsCount: Int,
    pathsSortBy: StringSorter.SortBy,
    pathsSortReverse: Boolean,
    songsSortBy: SongRepository.SortBy,
    songsSortReverse: Boolean,
    setPathsSortBy: (StringSorter.SortBy) -> Unit,
    setPathsSortReverse: (Boolean) -> Unit,
    setSongsSortBy: (SongRepository.SortBy) -> Unit,
    setSongsSortReverse: (Boolean) -> Unit,
) {
    val currentTextStyle = MaterialTheme.typography.bodySmall.run {
        copy(color = MaterialTheme.colorScheme.onSurface)
    }
    var showSortMenu by remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp, 4.dp)
                .clip(RoundedCornerShape(100))
                .clickable {
                    showSortMenu = !showSortMenu
                }
                .padding(8.dp, 8.dp)
        ) {
            ProvideTextStyle(value = currentTextStyle) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 2.dp),
                ) {
                    Icon(
                        when {
                            pathsSortReverse -> Icons.Filled.ArrowDownward
                            else -> Icons.Filled.ArrowUpward
                        },
                        null,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(pathsSortBy.label(context))
                    HorizontalDivider(
                        modifier = Modifier
                            .size(9.dp, 12.dp)
                            .padding(4.dp, 0.dp)
                    )
                    Icon(
                        when {
                            songsSortReverse -> Icons.Filled.ArrowDownward
                            else -> Icons.Filled.ArrowUpward
                        },
                        null,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(songsSortBy.label(context))
                }
            }
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = {
                    showSortMenu = false
                },
            ) {
                Row {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            context.symphony.t.Folders,
                            style = currentTextStyle,
                            modifier = Modifier.padding(16.dp, 8.dp),
                        )
                        StringSorter.SortBy.entries.forEach { sortBy ->
                            SongTreeListMediaSortBarDropdownMenuItem(
                                selected = pathsSortBy == sortBy,
                                reversed = pathsSortReverse,
                                text = { Text(sortBy.label(context)) },
                                onClick = {
                                    when (pathsSortBy) {
                                        sortBy -> setPathsSortReverse(!pathsSortReverse)
                                        else -> setPathsSortBy(sortBy)
                                    }
                                },
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            context.symphony.t.Songs,
                            style = currentTextStyle,
                            modifier = Modifier.padding(16.dp, 8.dp),
                        )
                        SongRepository.SortBy.entries.forEach { sortBy ->
                            SongTreeListMediaSortBarDropdownMenuItem(
                                selected = songsSortBy == sortBy,
                                reversed = songsSortReverse,
                                text = { Text(sortBy.label(context)) },
                                onClick = {
                                    when (songsSortBy) {
                                        sortBy -> setSongsSortReverse(!songsSortReverse)
                                        else -> setSongsSortBy(sortBy)
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
        Text(
            context.symphony.t.XSongs(songsCount.toString()),
            style = currentTextStyle,
            modifier = Modifier.padding(16.dp, 0.dp),
        )
    }
}

@Composable
private fun SongTreeListMediaSortBarDropdownMenuItem(
    selected: Boolean,
    reversed: Boolean,
    text: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        contentPadding = MenuDefaults.DropdownMenuItemContentPadding.run {
            val horizontalPadding = calculateLeftPadding(LayoutDirection.Ltr)
            PaddingValues(
                start = horizontalPadding.div(2),
                end = horizontalPadding.times(4),
            )
        },
        leadingIcon = {
            when {
                selected -> IconButton(
                    content = {
                        Icon(
                            when {
                                reversed -> Icons.Filled.ArrowCircleDown
                                else -> Icons.Filled.ArrowCircleUp
                            },
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    onClick = onClick,
                )

                else -> RadioButton(
                    selected = false,
                    onClick = onClick,
                )
            }
        },
        text = text,
        onClick = onClick,
    )
}

fun StringSorter.SortBy.label(context: ViewContext) = when (this) {
    StringSorter.SortBy.CUSTOM -> context.symphony.t.Custom
    StringSorter.SortBy.NAME -> context.symphony.t.Name
}

private fun createLinearTree(
    context: ViewContext,
    songIds: List<String>,
): Map<String, List<String>> {
    val result = mutableMapOf<String, MutableList<String>>()
    songIds.forEach { songId ->
        val song = context.symphony.groove.song.get(songId) ?: return@forEach
        val parsedPath = SimplePath(song.path)
        val dirname = parsedPath.parent!!.pathString
        if (!result.containsKey(dirname)) {
            result[dirname] = mutableListOf()
        }
        result[dirname]!!.add(songId)
    }
    return result
}
