package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import io.github.zyrouge.symphony.ui.components.AlbumGrid
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun AlbumsView(context: ViewContext, data: HomeViewData) {
    AlbumGrid(context, data.albums)
}
