package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.components.SongList
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun SongsView(context: ViewContext) {
    val isUpdating by context.symphony.groove.exposer.isUpdating.collectAsStateWithLifecycle()
    val sortBy by context.symphony.settings.lastUsedSongsSortBy.flow.collectAsStateWithLifecycle()
    val sortReverse by context.symphony.settings.lastUsedSongsSortReverse.flow.collectAsStateWithLifecycle()
    val songs by context.symphony.groove.song.valuesAsFlow(sortBy, sortReverse)
        .collectAsStateWithLifecycle(emptyList())

    LoaderScaffold(context, isLoading = isUpdating) {
        SongList(
            context,
            songs = songs,
            sortBy = sortBy,
            sortReverse = sortReverse,
            enableAddMediaFoldersHint = true,
        )
    }
}
