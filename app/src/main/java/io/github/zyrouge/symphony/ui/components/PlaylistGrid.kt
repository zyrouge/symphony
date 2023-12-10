package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import io.github.zyrouge.symphony.services.groove.GrooveKinds
import io.github.zyrouge.symphony.services.groove.PlaylistSortBy
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.wrapInViewContext

@Composable
fun PlaylistGrid(
    context: ViewContext,
    playlistIds: List<String>,
    playlistsCount: Int? = null,
    leadingContent: @Composable () -> Unit = {},
) {
    val sortBy by context.symphony.settings.lastUsedPlaylistsSortBy.collectAsState()
    val sortReverse by context.symphony.settings.lastUsedPlaylistsSortReverse.collectAsState()
    val sortedPlaylistIds by remember(playlistIds, sortBy, sortReverse) {
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
                        context.symphony.settings.setLastUsedPlaylistsSortReverse(it)
                    },
                    sort = sortBy,
                    sorts = PlaylistSortBy.entries
                        .associateWith { x -> wrapInViewContext { x.label(it) } },
                    onSortChange = {
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
                            Icons.AutoMirrored.Filled.QueueMusic,
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
