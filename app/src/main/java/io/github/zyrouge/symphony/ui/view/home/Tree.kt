package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.components.SongTreeList
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun TreeView(context: ViewContext, data: HomeViewData) {
    LoaderScaffold(context, isLoading = data.songsIsUpdating) {
        SongTreeList(
            context,
            songs = data.songs,
            initialDisabled = context.symphony.settings.getLastDisabledTreePaths(),
            onDisable = { paths ->
                context.symphony.settings.setLastDisabledTreePaths(paths)
            },
        )
    }
}
