package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.components.ArtistCard
import io.github.zyrouge.symphony.ui.components.ResponsiveGrid
import io.github.zyrouge.symphony.ui.view.helpers.ViewContext

@Composable
fun ArtistsView(context: ViewContext) {
    val artists = Symphony.groove.artist.cached.values.toList()
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        ResponsiveGrid(context, count = artists.size) { i ->
            ArtistCard(context, artists[i])
        }
    }
}
