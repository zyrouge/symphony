package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.components.SongTreeList
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun TreeView(context: ViewContext) {
    val isUpdating by context.symphony.groove.song.isUpdating.collectAsState()
    val songs by context.symphony.groove.song.all.collectAsState()

    LoaderScaffold(context, isLoading = isUpdating) {
        SongTreeList(
            context,
            songs = songs,
            initialDisabled = context.symphony.settings.getLastDisabledTreePaths(),
            onDisable = { paths ->
                context.symphony.settings.setLastDisabledTreePaths(paths)
            },
        )
    }
}
