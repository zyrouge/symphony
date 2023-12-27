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
import io.github.zyrouge.symphony.services.groove.ArtistSortBy
import io.github.zyrouge.symphony.services.groove.GrooveKinds
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.wrapInViewContext

@Composable
fun ArtistGrid(
    context: ViewContext,
    artistName: List<String>,
    artistsCount: Int? = null,
) {
    val sortBy by context.symphony.settings.lastUsedArtistsSortBy.collectAsState()
    val sortReverse by context.symphony.settings.lastUsedArtistsSortReverse.collectAsState()
    val sortedArtistNames by remember(artistName, sortBy, sortReverse) {
        derivedStateOf {
            context.symphony.groove.artist.sort(artistName, sortBy, sortReverse)
        }
    }

    MediaSortBarScaffold(
        mediaSortBar = {
            MediaSortBar(
                context,
                reverse = sortReverse,
                onReverseChange = {
                    context.symphony.settings.setLastUsedArtistsSortReverse(it)
                },
                sort = sortBy,
                sorts = ArtistSortBy.entries
                    .associateWith { x -> wrapInViewContext { x.label(it) } },
                onSortChange = {
                    context.symphony.settings.setLastUsedArtistsSortBy(it)
                },
                label = {
                    Text(context.symphony.t.XArtists((artistsCount ?: artistName.size).toString()))
                },
            )
        },
        content = {
            when {
                artistName.isEmpty() -> IconTextBody(
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
                        sortedArtistNames,
                        key = { i, x -> "$i-$x" },
                        contentType = { _, _ -> GrooveKinds.ARTIST }
                    ) { _, artistName ->
                        context.symphony.groove.artist.get(artistName)?.let { artist ->
                            ArtistTile(context, artist)
                        }
                    }
                }
            }
        }
    )
}

private fun ArtistSortBy.label(context: ViewContext) = when (this) {
    ArtistSortBy.CUSTOM -> context.symphony.t.Custom
    ArtistSortBy.ARTIST_NAME -> context.symphony.t.Artist
    ArtistSortBy.ALBUMS_COUNT -> context.symphony.t.AlbumCount
    ArtistSortBy.TRACKS_COUNT -> context.symphony.t.TrackCount
}
