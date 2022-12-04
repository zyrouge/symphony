package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.zyrouge.symphony.ui.components.AlbumGrid
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun AlbumsView(context: ViewContext, data: HomeViewData) {
    when {
        data.albums.isNotEmpty() -> AlbumGrid(
            context,
            data.albums,
            isLoading = data.albumsIsUpdating,
        )
        else -> IconTextBody(
            icon = { modifier ->
                Icon(
                    Icons.Default.Album,
                    null,
                    modifier = modifier,
                )
            },
            content = { Text(context.symphony.t.damnThisIsSoEmpty) }
        )
    }
}
