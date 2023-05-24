package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import io.github.zyrouge.symphony.services.groove.GrooveKinds
import io.github.zyrouge.symphony.services.groove.PlaylistSortBy
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun PlaylistGrid(
    context: ViewContext,
    playlistIds: List<String>,
    playlistsCount: Int? = null,
    leadingContent: @Composable () -> Unit = {},
) {
    var sortBy by remember {
        mutableStateOf(
            context.symphony.settings.getLastUsedPlaylistsSortBy() ?: PlaylistSortBy.TITLE,
        )
    }
    var sortReverse by remember {
        mutableStateOf(context.symphony.settings.getLastUsedPlaylistsSortReverse())
    }
    val sortedPlaylistIds by remember {
        derivedStateOf {
            context.symphony.groove.playlist.sort(playlistIds, sortBy, sortReverse)
        }
    }

    MediaSortBarScaffold(
        mediaSortBar = {
            Column {
                leadingContent()
                MediaSortBar(
                    context,
                    reverse = sortReverse,
                    onReverseChange = {
                        sortReverse = it
                        context.symphony.settings.setLastUsedPlaylistsSortReverse(it)
                    },
                    sort = sortBy,
                    sorts = PlaylistSortBy.values().associateWith { x -> { x.label(it) } },
                    onSortChange = {
                        sortBy = it
                        context.symphony.settings.setLastUsedPlaylistsSortBy(it)
                    },
                    label = {
                        Text(
                            context.symphony.t.XPlaylists(
                                (playlistsCount ?: playlistIds.size).toString()
                            )
                        )
                    },
                )
            }
        },
        content = {
            when {
                playlistIds.isEmpty() -> IconTextBody(
                    icon = { modifier ->
                        Icon(
                            Icons.Default.QueueMusic,
                            null,
                            modifier = modifier,
                        )
                    },
                    content = {
                        Text(context.symphony.t.DamnThisIsSoEmpty)
                    }
                )
                else -> ResponsiveGrid {
                    itemsIndexed(
                        sortedPlaylistIds,
                        key = { i, x -> "$i-$x" },
                        contentType = { _, _ -> GrooveKinds.PLAYLIST }
                    ) { _, playlistId ->
                        context.symphony.groove.playlist.get(playlistId)?.let { playlist ->
                            PlaylistTile(context, playlist)
                        }
                    }
                }
            }
        }
    )
}

private fun PlaylistSortBy.label(context: ViewContext) = when (this) {
    PlaylistSortBy.CUSTOM -> context.symphony.t.Custom
    PlaylistSortBy.TITLE -> context.symphony.t.Title
    PlaylistSortBy.TRACKS_COUNT -> context.symphony.t.TrackCount
}
