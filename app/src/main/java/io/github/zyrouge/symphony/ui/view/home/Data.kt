package io.github.zyrouge.symphony.ui.view.home

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.services.groove.Artist
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.utils.EventUnsubscribeFn
import io.github.zyrouge.symphony.utils.swap

class HomeViewData(val symphony: Symphony) {
    val songs = mutableStateListOf<Song>().apply { swap(symphony.groove.song.getAll()) }
    val artists = mutableStateListOf<Artist>().apply { swap(symphony.groove.artist.getAll()) }
    val albums = mutableStateListOf<Album>().apply { swap(symphony.groove.album.getAll()) }

    private var songsSubscriber: EventUnsubscribeFn? = null
    private var artistsSubscriber: EventUnsubscribeFn? = null
    private var albumsSubscriber: EventUnsubscribeFn? = null

    init {
        songsSubscriber = symphony.groove.song.onUpdate.subscribe {
            songs.swap(symphony.groove.song.getAll())
        }
        artistsSubscriber = symphony.groove.song.onUpdate.subscribe {
            artists.swap(symphony.groove.artist.getAll())
        }
        albumsSubscriber = symphony.groove.song.onUpdate.subscribe {
            albums.swap(symphony.groove.album.getAll())
        }
        Log.i("SymLog", "data created!")
    }

    fun dispose() {
        songsSubscriber?.invoke()
        artistsSubscriber?.invoke()
        albumsSubscriber?.invoke()
    }
}
