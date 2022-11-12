package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import io.github.zyrouge.symphony.ui.components.SongList
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun SongsView(context: ViewContext, data: HomeViewData) {
    SongList(context, data.songs)
}
