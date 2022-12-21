package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import io.github.zyrouge.symphony.services.groove.GrooveKinds
import io.github.zyrouge.symphony.services.groove.Playlist
import io.github.zyrouge.symphony.services.groove.PlaylistRepository
import io.github.zyrouge.symphony.services.groove.PlaylistSortBy
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun PlaylistGrid(
    context: ViewContext,
    playlists: List<Playlist>,
    isLoading: Boolean = false,
) {
    var sortBy by remember {
        mutableStateOf(
            context.symphony.settings.getLastUsedPlaylistsSortBy() ?: PlaylistSortBy.TITLE,
        )
    }
    var sortReverse by remember {
        mutableStateOf(context.symphony.settings.getLastUsedPlaylistsSortReverse())
    }
    val sortedPlaylists by remember {
        derivedStateOf { PlaylistRepository.sort(playlists, sortBy, sortReverse) }
    }

    ResponsiveGrid(
        topBar = {
            MediaSortBar(
                context,
                reverse = sortReverse,
                onReverseChange = {
                    sortReverse = it
                    context.symphony.settings.setLastUsedAlbumsSortReverse(it)
                },
                sort = sortBy,
                sorts = PlaylistSortBy.values().associateWith { x -> { x.label(it) } },
                onSortChange = {
                    sortBy = it
                    context.symphony.settings.setLastUsedPlaylistsSortBy(it)
                },
                label = {
                    Text(context.symphony.t.XAlbums(playlists.size))
                },
                isLoading = isLoading,
            )
        },
        content = {
            items(
                sortedPlaylists,
                key = { it.id },
                contentType = { GrooveKinds.PLAYLIST }
            ) { playlist ->
                PlaylistTile(context, playlist)
            }
        }
    )
}

private fun PlaylistSortBy.label(context: ViewContext) = when (this) {
    PlaylistSortBy.TITLE -> context.symphony.t.title
    PlaylistSortBy.TRACKS_COUNT -> context.symphony.t.trackCount
}
