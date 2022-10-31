package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.components.EventerEffect
import io.github.zyrouge.symphony.ui.components.SongList
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.swap

@Composable
fun SongsView(context: ViewContext) {
    val songs = remember {
        mutableStateListOf<Song>().apply {
            swap(context.symphony.groove.song.getAll())
        }
    }

    EventerEffect(context.symphony.groove.song.onUpdate) {
        songs.swap(context.symphony.groove.song.getAll())
    }

    SongList(context, songs)
}
