package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import io.github.zyrouge.symphony.services.groove.ArtistSortBy
import io.github.zyrouge.symphony.services.groove.GrooveKinds
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.contextWrapped

@Composable
fun ArtistGrid(
    context: ViewContext,
    artistIds: List<String>,
    artistsCount: Int? = null,
) {
    val sortBy by context.symphony.settings.lastUsedArtistsSortBy.collectAsState()
    val sortReverse by context.symphony.settings.lastUsedArtistsSortReverse.collectAsState()
    val sortedArtistIds by remember {
        derivedStateOf {
            context.symphony.groove.artist.sort(artistIds, sortBy, sortReverse)
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
                sorts = ArtistSortBy.values()
                    .associateWith { x -> contextWrapped { x.label(it) } },
                onSortChange = {
                    context.symphony.settings.setLastUsedArtistsSortBy(it)
                },
                label = {
                    Text(context.symphony.t.XArtists((artistsCount ?: artistIds.size).toString()))
                },
            )
        },
        content = {
            when {
                artistIds.isEmpty() -> IconTextBody(
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
                        sortedArtistIds,
                        key = { i, x -> "$i-$x" },
                        contentType = { _, _ -> GrooveKinds.ARTIST }
                    ) { _, artistId ->
                        context.symphony.groove.artist.get(artistId)?.let { artist ->
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
