package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import io.github.zyrouge.symphony.services.groove.GrooveKinds
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun AlbumRow(context: ViewContext, albumIds: List<String>) {
    BoxWithConstraints {
        val maxSize = min(maxHeight, maxWidth).div(2f)
        val width = min(maxSize, 200.dp)

        LazyRow {
            itemsIndexed(
                albumIds,
                key = { i, x -> "$i-$x" },
                contentType = { _, _ -> GrooveKinds.ALBUM }
            ) { _, albumId ->
                context.symphony.groove.album.get(albumId)?.let { album ->
                    Box(modifier = Modifier.width(width)) {
                        AlbumTile(context, album)
                    }
                }
            }
        }
    }
}
