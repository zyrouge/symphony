package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.components.SongTreeList
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun TreeView(context: ViewContext, data: HomeViewData) {
    LoaderScaffold(
        context,
        isLoading = data.songsIsUpdating,
    ) {
        when {
            data.songs.isNotEmpty() -> SongTreeList(
                context,
                songs = data.songs.toList(),
                initialDisabled = context.symphony.settings.getLastDisabledTreePaths(),
                onDisable = { paths ->
                    context.symphony.settings.setLastDisabledTreePaths(paths)
                },
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
