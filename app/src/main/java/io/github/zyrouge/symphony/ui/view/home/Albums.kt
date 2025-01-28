package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.zyrouge.symphony.ui.components.AlbumGrid
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.transformLatest

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun AlbumsView(context: ViewContext) {
    val isUpdating by context.symphony.groove.exposer.isUpdating.collectAsState()
    val sortBy by context.symphony.settings.lastUsedAlbumsSortBy.flow.collectAsState()
    val sortReverse by context.symphony.settings.lastUsedAlbumsSortReverse.flow.collectAsState()
    val albums by context.symphony.groove.album.valuesAsFlow(sortBy, sortReverse)
        .transformLatest { emit(it.map { x -> x.album }) }
        .collectAsState(emptyList())

    LoaderScaffold(context, isLoading = isUpdating) {
        AlbumGrid(
            context,
            albums = albums,
            sortBy = sortBy,
            sortReverse = sortReverse,
        )
    }
}
