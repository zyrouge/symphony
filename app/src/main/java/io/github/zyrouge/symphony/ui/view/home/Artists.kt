package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import io.github.zyrouge.symphony.ui.components.ArtistGrid
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun ArtistsView(context: ViewContext) {
    val isUpdating by context.symphony.groove.exposer.isUpdating.collectAsStateWithLifecycle()
    val sortBy by context.symphony.settings.lastUsedArtistsSortBy.flow.collectAsStateWithLifecycle()
    val sortReverse by context.symphony.settings.lastUsedArtistsSortReverse.flow.collectAsStateWithLifecycle()
    val artists by context.symphony.groove.artist.valuesAsFlow(sortBy, sortReverse)
        .mapLatest { it.map { x -> x.entity } }
        .collectAsStateWithLifecycle(emptyList())

    LoaderScaffold(context, isLoading = isUpdating) {
        ArtistGrid(
            context,
            artists = artists,
            sortBy = sortBy,
            sortReverse = sortReverse,
        )
    }
}
