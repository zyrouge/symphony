package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.zyrouge.symphony.ui.components.GenreGrid
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun GenresView(context: ViewContext) {
    val isUpdating by context.symphony.groove.exposer.isUpdating.collectAsState()
    val sortBy by context.symphony.settings.lastUsedGenresSortBy.flow.collectAsState()
    val sortReverse by context.symphony.settings.lastUsedGenresSortReverse.flow.collectAsState()
    val attributedGenres by context.symphony.groove.genre.valuesAsFlow(sortBy, sortReverse)
        .collectAsState(emptyList())

    LoaderScaffold(context, isLoading = isUpdating) {
        GenreGrid(
            context,
            attributedGenres = attributedGenres,
            sortBy = sortBy,
            sortReverse = sortReverse,
        )
    }
}
