package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.zyrouge.symphony.ui.components.GenreGrid
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun GenresView(context: ViewContext) {
    val isUpdating by context.symphony.groove.genre.isUpdating.collectAsState()
    val genreIds = context.symphony.groove.genre.all

    LoaderScaffold(context, isLoading = isUpdating) {
        GenreGrid(
            context,
            genreIds = genreIds,
            genresCount = genreIds.size,
        )
    }
}
