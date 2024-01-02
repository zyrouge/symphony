package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import io.github.zyrouge.symphony.services.groove.AlbumArtistSortBy
import io.github.zyrouge.symphony.services.groove.GrooveKinds
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.wrapInViewContext

@Composable
fun AlbumArtistGrid(
    context: ViewContext,
    albumArtistNames: List<String>,
    albumArtistsCount: Int? = null,
) {
    val sortBy by context.symphony.settings.lastUsedAlbumArtistsSortBy.collectAsState()
    val sortReverse by context.symphony.settings.lastUsedAlbumArtistsSortReverse.collectAsState()
    val sortedAlbumArtistNames by remember(albumArtistNames, sortBy, sortReverse) {
        derivedStateOf {
            context.symphony.groove.albumArtist.sort(albumArtistNames, sortBy, sortReverse)
        }
    }

    MediaSortBarScaffold(
        mediaSortBar = {
            MediaSortBar(
                context,
                reverse = sortReverse,
                onReverseChange = {
                    context.symphony.settings.setLastUsedAlbumArtistsSortReverse(it)
                },
                sort = sortBy,
                sorts = AlbumArtistSortBy.entries
                    .associateWith { x -> wrapInViewContext { x.label(context) } },
                onSortChange = {
                    context.symphony.settings.setLastUsedAlbumArtistsSortBy(it)
                },
                label = {
                    Text(
                        context.symphony.t.XArtists(
                            (albumArtistsCount ?: albumArtistNames.size).toString()
                        )
                    )
                },
            )
        },
        content = {
            when {
                albumArtistNames.isEmpty() -> IconTextBody(
                    icon = { modifier ->
                        Icon(
                            Icons.Filled.Person,
                            null,
                            modifier = modifier,
                        )
                    },
                    content = { Text(context.symphony.t.DamnThisIsSoEmpty) }
                )

                else -> ResponsiveGrid {
                    itemsIndexed(
                        sortedAlbumArtistNames,
                        key = { i, x -> "$i-$x" },
                        contentType = { _, _ -> GrooveKinds.ARTIST }
                    ) { _, albumArtistName ->
                        context.symphony.groove.albumArtist.get(albumArtistName)
                            ?.let { albumArtist ->
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
