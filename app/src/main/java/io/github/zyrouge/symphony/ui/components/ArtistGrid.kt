package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import io.github.zyrouge.symphony.services.groove.Artist
import io.github.zyrouge.symphony.services.groove.ArtistRepository
import io.github.zyrouge.symphony.services.groove.ArtistSortBy
import io.github.zyrouge.symphony.services.groove.GrooveKinds
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun ArtistGrid(
    context: ViewContext,
    artists: List<Artist>,
) {
    var sortBy by remember {
        mutableStateOf(
            context.symphony.settings.getLastUsedArtistsSortBy() ?: ArtistSortBy.ARTIST_NAME
        )
    }
    var sortReverse by remember {
        mutableStateOf(context.symphony.settings.getLastUsedArtistsSortReverse())
    }
    val sortedArtists by remember {
        derivedStateOf { ArtistRepository.sort(artists, sortBy, sortReverse) }
    }

    MediaSortBarScaffold(
        mediaSortBar = {
            MediaSortBar(
                context,
                reverse = sortReverse,
                onReverseChange = {
                    sortReverse = it
                    context.symphony.settings.setLastUsedArtistsSortReverse(it)
                },
                sort = sortBy,
                sorts = ArtistSortBy.values().associateWith { x -> { x.label(it) } },
                onSortChange = {
                    sortBy = it
                    context.symphony.settings.setLastUsedArtistsSortBy(it)
                },
                label = {
                    Text(context.symphony.t.XArtists(artists.size.toString()))
                },
            )
        },
        content = {
            ResponsiveGrid {
                itemsIndexed(
                    sortedArtists,
                    key = { i, x -> "$i-${x.name}" },
                    contentType = { _, _ -> GrooveKinds.ARTIST }
                ) { _, artist ->
                    ArtistTile(context, artist)
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
