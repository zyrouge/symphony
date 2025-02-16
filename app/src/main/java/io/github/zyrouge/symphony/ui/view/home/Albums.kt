package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import io.github.zyrouge.symphony.ui.components.AlbumGrid
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun AlbumsView(context: ViewContext) {
    val isUpdating by context.symphony.groove.exposer.isUpdating.collectAsStateWithLifecycle()
    val sortBy by context.symphony.settings.lastUsedAlbumsSortBy.flow.collectAsStateWithLifecycle()
    val sortReverse by context.symphony.settings.lastUsedAlbumsSortReverse.flow.collectAsStateWithLifecycle()
    val albums by context.symphony.groove.album.valuesAsFlow(sortBy, sortReverse)
        .mapLatest { it.map { x -> x.entity } }
        .collectAsStateWithLifecycle(emptyList())

    LoaderScaffold(context, isLoading = isUpdating) {
        AlbumGrid(
            context,
            albums = albums,
            sortBy = sortBy,
            sortReverse = sortReverse,
        )
    }
}
