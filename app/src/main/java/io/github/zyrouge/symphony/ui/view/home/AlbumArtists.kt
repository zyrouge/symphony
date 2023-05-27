package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.zyrouge.symphony.ui.components.AlbumArtistGrid
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun AlbumArtistsView(context: ViewContext) {
    val isUpdating by context.symphony.groove.albumArtist.isUpdating.collectAsState()
    val albumArtistIds = context.symphony.groove.albumArtist.all

    LoaderScaffold(context, isLoading = isUpdating) {
        AlbumArtistGrid(
            context,
            albumArtistIds = albumArtistIds,
            albumArtistsCount = albumArtistIds.size,
        )
    }
}
