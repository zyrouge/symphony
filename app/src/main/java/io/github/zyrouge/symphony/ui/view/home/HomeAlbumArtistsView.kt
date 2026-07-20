package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.zyrouge.symphony.ui.components.AlbumArtistGrid
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun HomeAlbumArtistsView(context: ViewContext) {
    val isUpdating by context.symphony.groove.exposer.isUpdating.collectAsStateWithLifecycle()
    val sortBy by context.symphony.settings.lastUsedAlbumArtistsSortBy.flow.collectAsStateWithLifecycle()
    val sortReverse by context.symphony.settings.lastUsedAlbumArtistsSortReverse.flow.collectAsStateWithLifecycle()
    val albumArtists by context.symphony.groove.artist
        .valuesAsFlow(sortBy, sortReverse, onlyAlbumArtists = true)
        .mapLatest { it.map { x -> x.entity } }
        .collectAsStateWithLifecycle(emptyList())

    LoaderScaffold(context, isLoading = isUpdating) {
        AlbumArtistGrid(
            context,
            albumArtists = albumArtists,
            sortBy = sortBy,
            sortReverse = sortReverse,
        )
    }
}
