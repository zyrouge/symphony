package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.components.SongList
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun SongsView(context: ViewContext, data: HomeViewData) {
    LoaderScaffold(context, isLoading = data.songsIsUpdating) {
        SongList(
            context,
            songs = data.songs,
            songsCount = data.songsCount,
        )
    }
}
