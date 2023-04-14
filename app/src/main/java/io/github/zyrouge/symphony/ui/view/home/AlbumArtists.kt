package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import io.github.zyrouge.symphony.ui.components.AlbumArtistGrid
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun AlbumArtistsView(context: ViewContext, data: HomeViewData) {
    LoaderScaffold(context, isLoading = data.albumArtistsIsUpdating) {
        AlbumArtistGrid(
            context,
            albumArtists = data.albumArtists,
            albumArtistsCount = data.albumArtistsCount,
        )
    }
}
