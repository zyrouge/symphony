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
    val genreNames by context.symphony.groove.genre.all.collectAsState()
    val genresCount by context.symphony.groove.genre.count.collectAsState()

    LoaderScaffold(context, isLoading = isUpdating) {
        GenreGrid(
            context,
            genreNames = genreNames,
            genresCount = genresCount,
        )
    }
}
