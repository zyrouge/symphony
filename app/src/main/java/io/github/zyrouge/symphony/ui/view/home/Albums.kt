package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.components.AlbumCard
import io.github.zyrouge.symphony.ui.components.ResponsiveGrid
import io.github.zyrouge.symphony.ui.view.helpers.ViewContext

@Composable
fun AlbumsView(context: ViewContext) {
    val albums = Symphony.groove.album.cached.values.toList()
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        ResponsiveGrid(context, count = albums.size) { i ->
            AlbumCard(context, albums[i])
        }
    }
}
