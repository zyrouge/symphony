package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import io.github.zyrouge.symphony.services.groove.AlbumArtistSortBy
import io.github.zyrouge.symphony.services.groove.GrooveKinds
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun AlbumArtistGrid(
    context: ViewContext,
    albumArtistIds: List<String>,
    albumArtistsCount: Int? = null,
) {
    var sortBy by remember {
        mutableStateOf(
            context.symphony.settings.getLastUsedAlbumArtistsSortBy()
                ?: AlbumArtistSortBy.ARTIST_NAME
        )
    }
    var sortReverse by remember {
        mutableStateOf(context.symphony.settings.getLastUsedAlbumArtistsSortReverse())
    }
    val sortedAlbumArtistIds by remember {
        derivedStateOf {
            context.symphony.groove.albumArtist.sort(albumArtistIds, sortBy, sortReverse)
        }
    }

    MediaSortBarScaffold(
        mediaSortBar = {
            MediaSortBar(
                context,
                reverse = sortReverse,
                onReverseChange = {
                    sortReverse = it
                    context.symphony.settings.setLastUsedAlbumArtistsSortReverse(it)
                },
                sort = sortBy,
                sorts = AlbumArtistSortBy.values().associateWith { x -> { x.label(it) } },
                onSortChange = {
                    sortBy = it
                    context.symphony.settings.setLastUsedAlbumArtistsSortBy(it)
                },
                label = {
                    Text(
                        context.symphony.t.XArtists(
                            (albumArtistsCount ?: albumArtistIds.size).toString()
                        )
                    )
                },
            )
        },
        content = {
            when {
                albumArtistIds.isEmpty() -> IconTextBody(
                    icon = { modifier ->
                        Icon(
                            Icons.Default.Person,
                            null,
                            modifier = modifier,
                        )
                    },
                    content = { Text(context.symphony.t.DamnThisIsSoEmpty) }
                )
                else -> ResponsiveGrid {
                    itemsIndexed(
                        sortedAlbumArtistIds,
                        key = { i, x -> "$i-$x" },
                        contentType = { _, _ -> GrooveKinds.ARTIST }
                    ) { _, albumArtistId ->
                        context.symphony.groove.albumArtist.get(albumArtistId)?.let { albumArtist ->
                            AlbumArtistTile(context, albumArtist)
                        }
                    }
                }
            }
        }
    )
}

private fun AlbumArtistSortBy.label(context: ViewContext) = when (this) {
    AlbumArtistSortBy.CUSTOM -> context.symphony.t.Custom
    AlbumArtistSortBy.ARTIST_NAME -> context.symphony.t.Artist
    AlbumArtistSortBy.ALBUMS_COUNT -> context.symphony.t.AlbumCount
    AlbumArtistSortBy.TRACKS_COUNT -> context.symphony.t.TrackCount
}
