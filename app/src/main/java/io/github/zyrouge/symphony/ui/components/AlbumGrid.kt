package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.services.groove.AlbumRepository
import io.github.zyrouge.symphony.services.groove.AlbumSortBy
import io.github.zyrouge.symphony.services.groove.GrooveKinds
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun AlbumGrid(
    context: ViewContext,
    albums: List<Album>,
) {
    var sortBy by remember {
        mutableStateOf(
            context.symphony.settings.getLastUsedAlbumsSortBy() ?: AlbumSortBy.ALBUM_NAME
        )
    }
    var sortReverse by remember {
        mutableStateOf(context.symphony.settings.getLastUsedAlbumsSortReverse())
    }
    val sortedAlbums by remember {
        derivedStateOf { AlbumRepository.sort(albums, sortBy, sortReverse) }
    }

    MediaSortBarScaffold(
        mediaSortBar = {
            MediaSortBar(
                context,
                reverse = sortReverse,
                onReverseChange = {
                    sortReverse = it
                    context.symphony.settings.setLastUsedAlbumsSortReverse(it)
                },
                sort = sortBy,
                sorts = AlbumSortBy.values().associateWith { x -> { x.label(it) } },
                onSortChange = {
                    sortBy = it
                    context.symphony.settings.setLastUsedAlbumsSortBy(it)
                },
                label = {
                    Text(context.symphony.t.XAlbums(albums.size))
                },
            )
        },
        content = {
            ResponsiveGrid {
                itemsIndexed(
                    sortedAlbums,
                    key = { i, x -> "$i-${x.id}" },
                    contentType = { _, _ -> GrooveKinds.ALBUM }
                ) { _, album ->
                    AlbumTile(context, album)
                }
            }
        }
    )
}

private fun AlbumSortBy.label(context: ViewContext) = when (this) {
    AlbumSortBy.CUSTOM -> context.symphony.t.custom
    AlbumSortBy.ALBUM_NAME -> context.symphony.t.album
    AlbumSortBy.ARTIST_NAME -> context.symphony.t.artist
    AlbumSortBy.TRACKS_COUNT -> context.symphony.t.trackCount
}
