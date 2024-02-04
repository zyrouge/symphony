package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.components.SongExplorerList
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun BrowserView(context: ViewContext) {
    val isUpdating by context.symphony.groove.song.isUpdating.collectAsState()
    val id by context.symphony.groove.song.id.collectAsState()
    val explorer = context.symphony.groove.song.explorer
    val lastUsedFolderPath by context.symphony.settings.lastUsedBrowserPath.collectAsState()

    LoaderScaffold(context, isLoading = isUpdating) {
        SongExplorerList(
            context,
            initialPath = lastUsedFolderPath,
            key = id,
            explorer = explorer,
            onPathChange = { path ->
                context.symphony.settings.setLastUsedBrowserPath(path)
            }
        )
    }
}
