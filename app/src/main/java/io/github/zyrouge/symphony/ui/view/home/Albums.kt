package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.zyrouge.symphony.ui.components.AlbumTile
import io.github.zyrouge.symphony.ui.components.EventerEffect
import io.github.zyrouge.symphony.ui.components.ResponsiveGrid
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun AlbumsView(context: ViewContext) {
    val albums = remember {
        mutableStateListOf(*context.symphony.groove.album.getAll().toTypedArray())
    }

    EventerEffect(context.symphony.groove.album.onUpdate) {
        albums.clear()
        albums.addAll(context.symphony.groove.album.getAll().toTypedArray())
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        ResponsiveGrid(albums.size) { i ->
            AlbumTile(context, albums[i])
        }
    }
}
