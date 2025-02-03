package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.components.SongList
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.transformLatest

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun SongsView(context: ViewContext) {
    val isUpdating by context.symphony.groove.exposer.isUpdating.collectAsState()
    val sortBy by context.symphony.settings.lastUsedSongsSortBy.flow.collectAsState()
    val sortReverse by context.symphony.settings.lastUsedSongsSortReverse.flow.collectAsState()
    val songs by context.symphony.groove.song.valuesAsFlow(sortBy, sortReverse)
        .transformLatest { emit(it.map { x -> x.song }) }
        .collectAsState(emptyList())

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
