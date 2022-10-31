package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import io.github.zyrouge.symphony.services.groove.Artist
import io.github.zyrouge.symphony.ui.components.ArtistGrid
import io.github.zyrouge.symphony.ui.components.EventerEffect
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.swap

@Composable
fun ArtistsView(context: ViewContext) {
    val artists = remember {
        mutableStateListOf<Artist>().apply {
            swap(context.symphony.groove.artist.getAll())
        }
    }

    EventerEffect(context.symphony.groove.artist.onUpdate) {
        artists.swap(context.symphony.groove.artist.getAll())
    }

    ArtistGrid(context, artists)
}
