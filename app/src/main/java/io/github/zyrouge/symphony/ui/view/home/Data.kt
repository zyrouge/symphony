package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.services.groove.Artist
import io.github.zyrouge.symphony.services.groove.Genre
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.utils.EventUnsubscribeFn
import io.github.zyrouge.symphony.utils.swap

class HomeViewData(val symphony: Symphony) {
    var songsIsUpdating by mutableStateOf(symphony.groove.song.isUpdating)
    val songs = mutableStateListOf<Song>().apply {
        swap(symphony.groove.song.getAll())
    }
    var songsExplorerId by mutableStateOf(System.currentTimeMillis())

    var artistsIsUpdating by mutableStateOf(symphony.groove.artist.isUpdating)
    val artists = mutableStateListOf<Artist>().apply {
        swap(symphony.groove.artist.getAll())
    }

    var albumsIsUpdating by mutableStateOf(symphony.groove.album.isUpdating)
    val albums = mutableStateListOf<Album>().apply {
        swap(symphony.groove.album.getAll())
    }

    var albumArtistsIsUpdating by mutableStateOf(symphony.groove.albumArtist.isUpdating)
    val albumArtists = mutableStateListOf<Artist>().apply {
        swap(symphony.groove.albumArtist.getAll())
    }

    var genresIsUpdating by mutableStateOf(symphony.groove.genre.isUpdating)
    val genres = mutableStateListOf<Genre>().apply {
        swap(symphony.groove.genre.getAll())
    }

    private var songsSubscriber: EventUnsubscribeFn? = null
    private var artistsSubscriber: EventUnsubscribeFn? = null
    private var albumsSubscriber: EventUnsubscribeFn? = null
    private var albumArtistsSubscriber: EventUnsubscribeFn? = null
    private var genresSubscriber: EventUnsubscribeFn? = null

    fun initialize() {
        updateAllStates()
        songsSubscriber = symphony.groove.song.onUpdate.subscribe { updateSongsState() }
        artistsSubscriber = symphony.groove.artist.onUpdate.subscribe { updateArtistsState() }
        albumsSubscriber = symphony.groove.album.onUpdate.subscribe { updateAlbumsState() }
        albumArtistsSubscriber =
            symphony.groove.albumArtist.onUpdate.subscribe { updateAlbumArtistsState() }
        genresSubscriber = symphony.groove.genre.onUpdate.subscribe { updateGenresState() }
    }

    fun dispose() {
        songsSubscriber?.invoke()
        artistsSubscriber?.invoke()
        albumsSubscriber?.invoke()
        albumArtistsSubscriber?.invoke()
        genresSubscriber?.invoke()
    }

    private fun updateAllStates() {
        updateSongsState()
        updateArtistsState()
        updateAlbumsState()
        updateAlbumArtistsState()
        updateGenresState()
    }

    private fun updateSongsState() {
        songsIsUpdating = symphony.groove.song.isUpdating
        songs.swap(symphony.groove.song.getAll())
        songsExplorerId = System.currentTimeMillis()
    }

    private fun updateArtistsState() {
        artistsIsUpdating = symphony.groove.artist.isUpdating
        artists.swap(symphony.groove.artist.getAll())
    }

    private fun updateAlbumsState() {
        albumsIsUpdating = symphony.groove.album.isUpdating
        albums.swap(symphony.groove.album.getAll())
    }

    private fun updateAlbumArtistsState() {
        albumArtistsIsUpdating = symphony.groove.albumArtist.isUpdating
        albumArtists.swap(symphony.groove.albumArtist.getAll())
    }

    private fun updateGenresState() {
        genresIsUpdating = symphony.groove.genre.isUpdating
        genres.swap(symphony.groove.genre.getAll())
    }
}
