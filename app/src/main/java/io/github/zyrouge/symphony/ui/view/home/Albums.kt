package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.zyrouge.symphony.ui.components.AlbumGrid
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun AlbumsView(context: ViewContext) {
    val isUpdating by context.symphony.groove.album.isUpdating.collectAsState()
    val albumIds by context.symphony.groove.album.all.collectAsState()
    val albumsCount by context.symphony.groove.album.count.collectAsState()

    LoaderScaffold(context, isLoading = isUpdating) {
        AlbumGrid(
            context,
            albumIds = albumIds,
            albumsCount = albumsCount,
        )
    }
}
