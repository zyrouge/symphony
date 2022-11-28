package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.mutableStateListOf
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.services.groove.Artist
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.utils.EventUnsubscribeFn
import io.github.zyrouge.symphony.utils.swap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class HomeViewData(val symphony: Symphony, val scope: CoroutineScope) {
    val songs = mutableStateListOf<Song>().apply { swap(symphony.groove.song.getAll()) }
    val artists = mutableStateListOf<Artist>().apply { swap(symphony.groove.artist.getAll()) }
    val albums = mutableStateListOf<Album>().apply { swap(symphony.groove.album.getAll()) }

    private var songsSubscriber: EventUnsubscribeFn? = null
    private var artistsSubscriber: EventUnsubscribeFn? = null
    private var albumsSubscriber: EventUnsubscribeFn? = null

    init {
        songsSubscriber = symphony.groove.song.onUpdate.subscribe {
            scope.launch { songs.swap(symphony.groove.song.getAll()) }
        }
        artistsSubscriber = symphony.groove.song.onUpdate.subscribe {
            scope.launch { artists.swap(symphony.groove.artist.getAll()) }
        }
        albumsSubscriber = symphony.groove.song.onUpdate.subscribe {
            scope.launch { albums.swap(symphony.groove.album.getAll()) }
        }
    }

    fun dispose() {
        songsSubscriber?.invoke()
        artistsSubscriber?.invoke()
        albumsSubscriber?.invoke()
    }
}
