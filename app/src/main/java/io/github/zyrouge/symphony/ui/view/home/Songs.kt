package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.components.SongList
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun SongsView(context: ViewContext) {
    val isUpdating by context.symphony.groove.song.isUpdating.collectAsState()
    val songIds = context.symphony.groove.song.all

    LoaderScaffold(context, isLoading = isUpdating) {
        SongList(
            context,
            songIds = songIds,
            songsCount = songIds.size,
        )
    }
}
