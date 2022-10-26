package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.services.groove.SongRepository
import io.github.zyrouge.symphony.services.groove.SongSortBy
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun SongList(
    context: ViewContext,
    songs: List<Song>,
    leadingContent: (LazyListScope.() -> Unit)? = null,
    trailingContent: (LazyListScope.() -> Unit)? = null,
) {
    var sortBy by remember { mutableStateOf(SongSortBy.TITLE) }
    var sortReverse by remember { mutableStateOf(false) }
    var sortedSongs by remember {
        mutableStateOf(SongRepository.sort(songs, sortBy, sortReverse))
    }

    fun sortSongsAgain() {
        sortedSongs = SongRepository.sort(songs, sortBy, sortReverse)
    }

    LazyColumn {
        leadingContent?.invoke(this)
        item {
            SongListBar(
                context,
                size = sortedSongs.size,
                sortBy = sortBy,
                sortReverse = sortReverse,
                onSortByChange = {
                    sortBy = it
                    sortSongsAgain()
                },
                onSortReverseChange = {
                    sortReverse = it
                    sortSongsAgain()
                }
            )
        }
        itemsIndexed(sortedSongs) { i, song ->
            SongCard(context, song) {
                context.symphony.player.stop()
                context.symphony.player.addToQueue(
                    sortedSongs.subList(i, sortedSongs.size).toList()
                )
            }
        }
        trailingContent?.invoke(this)
    }
}

@Composable
fun SongListBar(
    context: ViewContext,
    size: Int,
    sortBy: SongSortBy,
    sortReverse: Boolean,
    onSortByChange: (SongSortBy) -> Unit,
    onSortReverseChange: (Boolean) -> Unit,
) {
    var showDropdown by remember { mutableStateOf(false) }
    val currentTextStyle = MaterialTheme.typography.bodySmall.run {
        copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row {
            Box(
                modifier = Modifier.width(60.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = currentTextStyle.color
                    ),
                    onClick = {
                        onSortReverseChange(!sortReverse)
                    }
                ) {
                    Icon(
                        if (sortReverse) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        null,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Box {
                TextButton(
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = currentTextStyle.color
                    ),
                    onClick = {
                        showDropdown = !showDropdown
                    }
                ) {
                    Text(sortBy.label(context), style = currentTextStyle)
                }
                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = { showDropdown = false }
                ) {
                    SongSortBy.values().map {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    it.label(context),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            onClick = {
                                showDropdown = false
                                onSortByChange(it)
                            }
                        )
                    }
                }
            }
        }
        Text(
            context.symphony.t.XSongs(size),
            modifier = Modifier.padding(16.dp, 0.dp),
            style = currentTextStyle
        )
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
