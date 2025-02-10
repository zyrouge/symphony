package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.components.SongExplorerList
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.SimplePath

@Composable
fun BrowserView(context: ViewContext) {
    val isUpdating by context.symphony.groove.exposer.isUpdating.collectAsState()
    val explorer = context.symphony.groove.song.explorer
    val lastUsedFolderPath by context.symphony.settings.lastUsedBrowserPath.flow.collectAsState()

    LoaderScaffold(context, isLoading = isUpdating) {
        SongExplorerList(
            context,
            initialPath = lastUsedFolderPath?.let { SimplePath(it) },
            key = id,
            explorer = explorer,
            onPathChange = { path ->
                context.symphony.settings.lastUsedBrowserPath.setValue(path.pathString)
            }
        )
    }
}
