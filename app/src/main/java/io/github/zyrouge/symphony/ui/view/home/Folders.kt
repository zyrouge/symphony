package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.github.zyrouge.symphony.ui.components.ExplorerList
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun FoldersView(context: ViewContext, data: HomeViewData) {
    val folder by remember {
        mutableStateOf(data.songsExplorerId to context.symphony.groove.song.explorer)
    }
    when {
        !folder.second.isEmpty -> ExplorerList(
            context,
            initialPath = context.symphony.settings.getLastUsedFolderPath(),
            explorer = folder.second,
            isLoading = data.songsIsUpdating,
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
