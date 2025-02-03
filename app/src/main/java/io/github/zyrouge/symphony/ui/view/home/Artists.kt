package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.zyrouge.symphony.ui.components.ArtistGrid
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.transformLatest

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun ArtistsView(context: ViewContext) {
    val isUpdating by context.symphony.groove.exposer.isUpdating.collectAsState()
    val sortBy by context.symphony.settings.lastUsedArtistsSortBy.flow.collectAsState()
    val sortReverse by context.symphony.settings.lastUsedArtistsSortReverse.flow.collectAsState()
    val artists by context.symphony.groove.artist.valuesAsFlow(sortBy, sortReverse)
        .transformLatest { emit(it.map { x -> x.artist }) }
        .collectAsState(emptyList())

    LoaderScaffold(context, isLoading = isUpdating) {
        ArtistGrid(
            context,
            artists = artists,
            sortBy = sortBy,
            sortReverse = sortReverse,
        )
    }
}
