package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.zyrouge.symphony.ui.components.AlbumArtistGrid
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun AlbumArtistsView(context: ViewContext) {
    val isUpdating by context.symphony.groove.exposer.isUpdating.collectAsState()
    val sortBy by context.symphony.settings.lastUsedAlbumArtistsSortBy.flow.collectAsState()
    val sortReverse by context.symphony.settings.lastUsedAlbumArtistsSortReverse.flow.collectAsState()
    val albumArtists by context.symphony.groove.albumArtist.valuesAsFlow(sortBy, sortReverse)
        .mapLatest { it.map { x -> x.artist } }
        .collectAsState(emptyList())

    LoaderScaffold(context, isLoading = isUpdating) {
        AlbumArtistGrid(
            context,
            albumArtists = albumArtists,
            sortBy = sortBy,
            sortReverse = sortReverse,
        )
    }
}
