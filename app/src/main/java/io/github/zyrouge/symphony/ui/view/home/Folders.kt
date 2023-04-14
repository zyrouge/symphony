package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.components.SongExplorerList
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun FoldersView(context: ViewContext, data: HomeViewData) {
    LoaderScaffold(context, isLoading = data.songsIsUpdating) {
        SongExplorerList(
            context,
            initialPath = context.symphony.settings.getLastUsedFolderPath(),
            key = data.songsExplorerId,
            explorer = context.symphony.groove.song.explorer,
            onPathChange = { path ->
                context.symphony.settings.setLastUsedFolderPath(path)
            }
        )
    }
}
