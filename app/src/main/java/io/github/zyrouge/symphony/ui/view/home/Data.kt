package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.EventUnsubscribeFn

class HomeViewData(val symphony: Symphony) {
    var songsIsUpdating by mutableStateOf(symphony.groove.song.isUpdating)
    var songs by mutableStateOf(symphony.groove.song.getAll())
    var songsExplorerId by mutableStateOf(System.currentTimeMillis())

    var artistsIsUpdating by mutableStateOf(symphony.groove.artist.isUpdating)
    var artists by mutableStateOf(symphony.groove.artist.getAll())

    var albumsIsUpdating by mutableStateOf(symphony.groove.album.isUpdating)
    var albums by mutableStateOf(symphony.groove.album.getAll())

    var albumArtistsIsUpdating by mutableStateOf(symphony.groove.albumArtist.isUpdating)
    var albumArtists by mutableStateOf(symphony.groove.albumArtist.getAll())

    var genresIsUpdating by mutableStateOf(symphony.groove.genre.isUpdating)
    var genres by mutableStateOf(symphony.groove.genre.getAll())

    var playlistsIsUpdating by mutableStateOf(symphony.groove.playlist.isUpdating)
    var playlists by mutableStateOf(symphony.groove.playlist.getAll())

    private var songsSubscriber: EventUnsubscribeFn? = null
    private var artistsSubscriber: EventUnsubscribeFn? = null
    private var albumsSubscriber: EventUnsubscribeFn? = null
    private var albumArtistsSubscriber: EventUnsubscribeFn? = null
    private var genresSubscriber: EventUnsubscribeFn? = null
    private var playlistsSubscriber: EventUnsubscribeFn? = null

    fun initialize() {
        updateAllStates()
        songsSubscriber = symphony.groove.song.onUpdate.subscribe { updateSongsState() }
        artistsSubscriber = symphony.groove.artist.onUpdate.subscribe { updateArtistsState() }
        albumsSubscriber = symphony.groove.album.onUpdate.subscribe { updateAlbumsState() }
        albumArtistsSubscriber =
            symphony.groove.albumArtist.onUpdate.subscribe { updateAlbumArtistsState() }
        genresSubscriber = symphony.groove.genre.onUpdate.subscribe { updateGenresState() }
        playlistsSubscriber = symphony.groove.playlist.onUpdate.subscribe { updatePlaylistsState() }
    }

    fun dispose() {
        songsSubscriber?.invoke()
        artistsSubscriber?.invoke()
        albumsSubscriber?.invoke()
        albumArtistsSubscriber?.invoke()
        genresSubscriber?.invoke()
        playlistsSubscriber?.invoke()
    }

    private fun updateAllStates() {
        updateSongsState()
        updateArtistsState()
        updateAlbumsState()
        updateAlbumArtistsState()
        updateGenresState()
        updatePlaylistsState()
    }

    private fun updateSongsState() {
        songsIsUpdating = symphony.groove.song.isUpdating
        songs = symphony.groove.song.getAll()
        songsExplorerId = System.currentTimeMillis()
    }

    private fun updateArtistsState() {
        artistsIsUpdating = symphony.groove.artist.isUpdating
        artists = symphony.groove.artist.getAll()
    }

    private fun updateAlbumsState() {
        albumsIsUpdating = symphony.groove.album.isUpdating
        albums = symphony.groove.album.getAll()
    }

    private fun updateAlbumArtistsState() {
        albumArtistsIsUpdating = symphony.groove.albumArtist.isUpdating
        albumArtists = symphony.groove.albumArtist.getAll()
    }

    private fun updateGenresState() {
        genresIsUpdating = symphony.groove.genre.isUpdating
        genres = symphony.groove.genre.getAll()
    }

    private fun updatePlaylistsState() {
        playlistsIsUpdating = symphony.groove.playlist.isUpdating
        playlists = symphony.groove.playlist.getAll()
    }
}
