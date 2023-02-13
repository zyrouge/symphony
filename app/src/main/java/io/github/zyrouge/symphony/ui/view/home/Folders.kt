package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.components.SongExplorerList
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun FoldersView(context: ViewContext, data: HomeViewData) {
    val explorer = context.symphony.groove.song.explorer

    LoaderScaffold(context, isLoading = data.songsIsUpdating) {
        when {
            !explorer.isEmpty -> SongExplorerList(
                context,
                initialPath = context.symphony.settings.getLastUsedFolderPath(),
                key = data.songsExplorerId,
                explorer = explorer,
                onPathChange = { path ->
                    context.symphony.settings.setLastUsedFolderPath(path)
                }
            )
            else -> IconTextBody(
                icon = { modifier ->
                    Icon(
                        Icons.Default.MusicNote,
                        null,
                        modifier = modifier,
                    )
                },
                content = { Text(context.symphony.t.damnThisIsSoEmpty) }
            )
        }
    }
}
