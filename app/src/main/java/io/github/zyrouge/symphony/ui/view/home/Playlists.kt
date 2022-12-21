package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.components.PlaylistGrid
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun PlaylistsView(context: ViewContext, data: HomeViewData) {
    when {
        data.playlists.isNotEmpty() -> PlaylistGrid(
            context,
            data.playlists,
            isLoading = data.playlistsIsUpdating,
        )
        else -> IconTextBody(
            icon = { modifier ->
                Icon(
                    Icons.Default.QueueMusic,
                    null,
                    modifier = modifier,
                )
            },
            content = { Text(context.symphony.t.damnThisIsSoEmpty) }
        )
    }
}
