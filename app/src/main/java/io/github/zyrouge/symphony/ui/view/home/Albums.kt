package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.zyrouge.symphony.ui.components.AlbumGrid
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun AlbumsView(context: ViewContext, data: HomeViewData) {
    LoaderScaffold(context, isLoading = data.albumsIsUpdating) {
        val albums = data.albums

        when {
            albums.isNotEmpty() -> AlbumGrid(
                context,
                albums = albums,
            )
            else -> IconTextBody(
                icon = { modifier ->
                    Icon(
                        Icons.Default.Album,
                        null,
                        modifier = modifier,
                    )
                },
                content = { Text(context.symphony.t.DamnThisIsSoEmpty) }
            )
        }
    }
}
