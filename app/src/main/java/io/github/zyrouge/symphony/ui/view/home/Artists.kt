package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import io.github.zyrouge.symphony.ui.components.ArtistGrid
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun ArtistsView(context: ViewContext, data: HomeViewData) {
    ArtistGrid(context, data.artists)
}
