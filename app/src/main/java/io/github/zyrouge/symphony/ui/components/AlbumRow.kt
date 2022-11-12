package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.services.groove.GrooveKinds
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun AlbumRow(context: ViewContext, albums: List<Album>) {
    LazyRow {
        items(
            albums,
            key = { it.albumId },
            contentType = { GrooveKinds.ALBUM }
        ) { album ->
            AlbumTile(context, album)
        }
    }
}
