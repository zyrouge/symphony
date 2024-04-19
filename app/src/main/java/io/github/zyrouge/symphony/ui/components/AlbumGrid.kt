package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import io.github.zyrouge.symphony.services.groove.AlbumSortBy
import io.github.zyrouge.symphony.services.groove.GrooveKinds
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.wrapInViewContext

@Composable
fun AlbumGrid(
    context: ViewContext,
    albumIds: List<String>,
    albumsCount: Int? = null,
) {
    val sortBy by context.symphony.settings.lastUsedAlbumsSortBy.collectAsState()
    val sortReverse by context.symphony.settings.lastUsedAlbumsSortReverse.collectAsState()
    val sortedAlbumIds by remember(albumIds, sortBy, sortReverse) {
        derivedStateOf {
            context.symphony.groove.album.sort(albumIds, sortBy, sortReverse)
        }
    }

    MediaSortBarScaffold(
        mediaSortBar = {
            MediaSortBar(
                context,
                reverse = sortReverse,
                onReverseChange = {
                    context.symphony.settings.setLastUsedAlbumsSortReverse(it)
                },
                sort = sortBy,
                sorts = AlbumSortBy.entries.associateWith { x -> wrapInViewContext { x.label(it) } },
                onSortChange = {
                    context.symphony.settings.setLastUsedAlbumsSortBy(it)
                },
                label = {
                    Text(context.symphony.t.XAlbums((albumsCount ?: albumIds.size).toString()))
                },
            )
        },
        content = {
            when {
                albumIds.isEmpty() -> IconTextBody(
                    icon = { modifier ->
                        Icon(
                            Icons.Filled.Album,
                            null,
                            modifier = modifier,
                        )
                    },
                    content = { Text(context.symphony.t.DamnThisIsSoEmpty) }
                )

                else -> ResponsiveGrid {
                    itemsIndexed(
                        sortedAlbumIds,
                        key = { i, x -> "$i-$x" },
                        contentType = { _, _ -> GrooveKinds.ALBUM }
                    ) { _, albumId ->
                        context.symphony.groove.album.get(albumId)?.let { album ->
                            AlbumTile(context, album)
                        }
                    }
                }
            }
        }
    )
}

private fun AlbumSortBy.label(context: ViewContext) = when (this) {
    AlbumSortBy.CUSTOM -> context.symphony.t.Custom
    AlbumSortBy.ALBUM_NAME -> context.symphony.t.Album
    AlbumSortBy.ARTIST_NAME -> context.symphony.t.Artist
    AlbumSortBy.TRACKS_COUNT -> context.symphony.t.TrackCount
}
