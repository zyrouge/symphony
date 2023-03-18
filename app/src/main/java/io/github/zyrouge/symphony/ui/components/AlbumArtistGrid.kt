package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import io.github.zyrouge.symphony.services.groove.AlbumArtist
import io.github.zyrouge.symphony.services.groove.AlbumArtistRepository
import io.github.zyrouge.symphony.services.groove.AlbumArtistSortBy
import io.github.zyrouge.symphony.services.groove.GrooveKinds
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun AlbumArtistGrid(context: ViewContext, albumArtists: List<AlbumArtist>) {
    var sortBy by remember {
        mutableStateOf(
            context.symphony.settings.getLastUsedAlbumArtistsSortBy()
                ?: AlbumArtistSortBy.ARTIST_NAME
        )
    }
    var sortReverse by remember {
        mutableStateOf(context.symphony.settings.getLastUsedAlbumArtistsSortReverse())
    }
    val sortedAlbumArtists by remember {
        derivedStateOf { AlbumArtistRepository.sort(albumArtists, sortBy, sortReverse) }
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
                sorts = AlbumArtistSortBy.values().associateWith { x -> { x.label(it) } },
                onSortChange = {
                    sortBy = it
                    context.symphony.settings.setLastUsedAlbumArtistsSortBy(it)
                },
                label = {
                    Text(context.symphony.t.XArtists(albumArtists.size.toString()))
                },
            )
        },
        content = {
            ResponsiveGrid {
                itemsIndexed(
                    sortedAlbumArtists,
                    key = { i, x -> "$i-${x.name}" },
                    contentType = { _, _ -> GrooveKinds.ARTIST }
                ) { _, albumArtist ->
                    AlbumArtistTile(context, albumArtist)
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
