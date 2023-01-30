package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.lazy.grid.items
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
    isLoading: Boolean = false,
) {
    ArtistGrid(context, artists, isLoading = isLoading, isAlbumArtist = false)
}

@Composable
fun AlbumArtistGrid(
    context: ViewContext,
    artists: List<Artist>,
    isLoading: Boolean = false,
) {
    ArtistGrid(context, artists, isLoading = isLoading, isAlbumArtist = true)
}

@Composable
internal fun ArtistGrid(
    context: ViewContext,
    artists: List<Artist>,
    isLoading: Boolean = false,
    isAlbumArtist: Boolean,
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

    ResponsiveGrid(
        topBar = {
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
                    Text(context.symphony.t.XArtists(artists.size))
                },
                isLoading = isLoading,
            )
        },
        content = {
            items(
                sortedArtists,
                key = { it.name },
                contentType = { GrooveKinds.ARTIST }
            ) { artist ->
                ArtistTile(context, artist, isAlbumArtist)
            }
        }
    )
}

private fun ArtistSortBy.label(context: ViewContext) = when (this) {
    ArtistSortBy.CUSTOM -> context.symphony.t.custom
    ArtistSortBy.ARTIST_NAME -> context.symphony.t.artist
    ArtistSortBy.ALBUMS_COUNT -> context.symphony.t.albumCount
    ArtistSortBy.TRACKS_COUNT -> context.symphony.t.trackCount
}
