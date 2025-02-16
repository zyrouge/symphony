package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.zyrouge.symphony.services.groove.Groove
import io.github.zyrouge.symphony.services.groove.entities.Playlist
import io.github.zyrouge.symphony.services.groove.repositories.PlaylistRepository
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistGrid(
    context: ViewContext,
    playlists: List<Playlist>,
    sortBy: PlaylistRepository.SortBy,
    sortReverse: Boolean,
    leadingContent: @Composable () -> Unit = {},
) {
    val horizontalGridColumns by context.symphony.settings.lastUsedPlaylistsHorizontalGridColumns.flow.collectAsStateWithLifecycle()
    val verticalGridColumns by context.symphony.settings.lastUsedPlaylistsVerticalGridColumns.flow.collectAsStateWithLifecycle()
    val gridColumns by remember(horizontalGridColumns, verticalGridColumns) {
        derivedStateOf {
            ResponsiveGridColumns(horizontalGridColumns, verticalGridColumns)
        }
    }
    var showModifyLayoutSheet by remember { mutableStateOf(false) }

    MediaSortBarScaffold(
        mediaSortBar = {
            Column {
                leadingContent()
                MediaSortBar(
                    context,
                    reverse = sortReverse,
                    onReverseChange = {
                        context.symphony.settings.lastUsedPlaylistsSortReverse.setValue(it)
                    },
                    sort = sortBy,
                    sorts = PlaylistRepository.SortBy.entries
                        .associateWith { x -> ViewContext.parameterizedFn { x.label(it) } },
                    onSortChange = {
                        context.symphony.settings.lastUsedPlaylistsSortBy.setValue(it)
                    },
                    label = {
                        Text(context.symphony.t.XPlaylists(playlists.size.toString()))
                    },
                    onShowModifyLayout = {
                        showModifyLayoutSheet = true
                    },
                )
            }
        },
        content = {
            when {
                playlists.isEmpty() -> IconTextBody(
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

                else -> ResponsiveGrid(gridColumns) {
                    itemsIndexed(
                        playlists,
                        key = { i, x -> "$i-$x" },
                        contentType = { _, _ -> Groove.Kind.PLAYLIST }
                    ) { _, playlist ->
                        PlaylistTile(context, playlist)
                    }
                }
            }

            if (showModifyLayoutSheet) {
                ResponsiveGridSizeAdjustBottomSheet(
                    context,
                    columns = gridColumns,
                    onColumnsChange = {
                        context.symphony.settings.lastUsedPlaylistsHorizontalGridColumns.setValue(
                            it.horizontal
                        )
                        context.symphony.settings.lastUsedPlaylistsVerticalGridColumns.setValue(
                            it.vertical
                        )
                    },
                    onDismissRequest = {
                        showModifyLayoutSheet = false
                    }
                )
            }
        }
    )
}

private fun PlaylistRepository.SortBy.label(context: ViewContext) = when (this) {
    PlaylistRepository.SortBy.CUSTOM -> context.symphony.t.Custom
    PlaylistRepository.SortBy.TITLE -> context.symphony.t.Title
    PlaylistRepository.SortBy.TRACKS_COUNT -> context.symphony.t.TrackCount
}
