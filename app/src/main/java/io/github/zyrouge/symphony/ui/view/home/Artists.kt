package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.zyrouge.symphony.ui.components.ArtistTile
import io.github.zyrouge.symphony.ui.components.EventerEffect
import io.github.zyrouge.symphony.ui.components.ResponsiveGrid
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun ArtistsView(context: ViewContext) {
    val artists = remember {
        mutableStateListOf(*context.symphony.groove.artist.getAll().toTypedArray())
    }

    EventerEffect(context.symphony.groove.artist.onUpdate) {
        artists.clear()
        artists.addAll(context.symphony.groove.artist.getAll().toTypedArray())
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        ResponsiveGrid(artists.size) { i ->
            ArtistTile(context, artists[i])
        }
    }
}
