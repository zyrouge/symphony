package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import io.github.zyrouge.symphony.ui.components.GenreGrid
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun GenresView(context: ViewContext, data: HomeViewData) {
    LoaderScaffold(context, isLoading = data.genresIsUpdating) {
        GenreGrid(
            context,
            genres = data.genres,
            genresCount = data.genresCount,
        )
    }
}
