package io.github.zyrouge.symphony.ui.view.home

import android.util.Log
import androidx.compose.runtime.*
import io.github.zyrouge.symphony.ui.components.EventerEffect
import io.github.zyrouge.symphony.ui.components.SongList
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun SongsView(context: ViewContext) {
    var songs by remember {
        mutableStateOf(context.symphony.groove.song.getAll())
    }

    EventerEffect(context.symphony.groove.song.onUpdate) {
        songs = context.symphony.groove.song.getAll()
        Log.i("Symphony", "rebuild song list ${songs.size}")
    }

    SongList(context, songs)
}
