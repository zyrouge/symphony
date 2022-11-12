package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import io.github.zyrouge.symphony.services.groove.Artist
import io.github.zyrouge.symphony.services.groove.ArtistRepository
import io.github.zyrouge.symphony.services.groove.ArtistSortBy
import io.github.zyrouge.symphony.services.groove.GrooveKinds
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.swap
import kotlinx.coroutines.launch

@Composable
fun ArtistGrid(context: ViewContext, artists: List<Artist>) {
    val scope = rememberCoroutineScope()
    var sortBy by remember {
        mutableStateOf(
            context.symphony.settings.getLastUsedArtistsSortBy() ?: ArtistSortBy.ARTIST_NAME
        )
    }
    var sortReverse by remember {
        mutableStateOf(context.symphony.settings.getLastUsedArtistsSortReverse())
    }
    val sortedArtists = remember {
        mutableStateListOf<Artist>().apply {
            swap(ArtistRepository.sort(artists, sortBy, sortReverse))
        }
    }

    fun sortArtistsAgain() {
        sortedArtists.swap(ArtistRepository.sort(artists, sortBy, sortReverse))
    }

    LaunchedEffect(LocalContext.current) {
        scope.launch {
            snapshotFlow { artists.toList() }.collect { sortArtistsAgain() }
        }
    }

    ResponsiveGrid(
        topBar = {
            MediaSortBar(
                context,
                reverse = sortReverse,
                onReverseChange = {
                    sortReverse = it
                    sortArtistsAgain()
                    context.symphony.settings.setLastUsedArtistsSortReverse(it)
                },
                sort = sortBy,
                sorts = ArtistSortBy.values().associateWith { x -> { x.label(it) } },
                onSortChange = {
                    sortBy = it
                    sortArtistsAgain()
                    context.symphony.settings.setLastUsedArtistsSortBy(it)
                },
                label = {
                    Text(context.symphony.t.XArtists(artists.size))
                }
            )
        },
        content = {
            items(
                sortedArtists,
                key = { it.artistName },
                contentType = { GrooveKinds.ARTIST }
            ) { artist ->
                ArtistTile(context, artist)
            }
        }
    )
}

private fun ArtistSortBy.label(context: ViewContext) = when (this) {
    ArtistSortBy.ARTIST_NAME -> context.symphony.t.artist
    ArtistSortBy.ALBUMS_COUNT -> context.symphony.t.albumCount
    ArtistSortBy.TRACKS_COUNT -> context.symphony.t.trackCount
}
