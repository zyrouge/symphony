package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.ui.components.AlbumGrid
import io.github.zyrouge.symphony.ui.components.EventerEffect
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.swap

@Composable
fun AlbumsView(context: ViewContext) {
    val albums = remember {
        mutableStateListOf<Album>().apply {
            swap(context.symphony.groove.album.getAll())
        }
    }

    EventerEffect(context.symphony.groove.album.onUpdate) {
        albums.swap(context.symphony.groove.album.getAll())
    }

    AlbumGrid(context, albums)
}
