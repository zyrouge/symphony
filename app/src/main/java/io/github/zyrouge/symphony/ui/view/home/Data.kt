package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.*
import io.github.zyrouge.symphony.utils.EventUnsubscribeFn
import io.github.zyrouge.symphony.utils.addAllVarArg
import io.github.zyrouge.symphony.utils.asImmutableList
import io.github.zyrouge.symphony.utils.swap

class HomeViewData(val symphony: Symphony) {
    var songsIsUpdating by mutableStateOf(symphony.groove.song.isUpdating)
    var songsCount by mutableStateOf(0)
    val songsMutable = mutableStateListOf<Song>()
    val songs = songsMutable.asImmutableList()
    var songsExplorerId by mutableStateOf(System.currentTimeMillis())

    var artistsIsUpdating by mutableStateOf(symphony.groove.artist.isUpdating)
    var artistsCount by mutableStateOf(0)
    val artistsMutable = mutableStateListOf<Artist>()
    val artists = artistsMutable.asImmutableList()

    var albumsIsUpdating by mutableStateOf(symphony.groove.album.isUpdating)
    var albumsCount by mutableStateOf(0)
    val albumsMutable = mutableStateListOf<Album>()
    val albums = albumsMutable.asImmutableList()

    var albumArtistsIsUpdating by mutableStateOf(symphony.groove.albumArtist.isUpdating)
    var albumArtistsCount by mutableStateOf(0)
    val albumArtistsMutable = mutableStateListOf<AlbumArtist>()
    val albumArtists = albumArtistsMutable.asImmutableList()

    var genresIsUpdating by mutableStateOf(symphony.groove.genre.isUpdating)
    var genresCount by mutableStateOf(0)
    val genresMutable = mutableStateListOf<Genre>()
    val genres = genresMutable.asImmutableList()

    var playlistsIsUpdating by mutableStateOf(symphony.groove.playlist.isUpdating)
    var playlistsCount by mutableStateOf(0)
    val playlistsMutable = mutableStateListOf<Playlist>()
    val playlists = playlistsMutable.asImmutableList()

    private var subscribers = mutableListOf<EventUnsubscribeFn>()

    fun initialize() {
        updateAllStates()
        subscribers.addAllVarArg(
            symphony.groove.song.onUpdate.subscribe {
                updateSongsState(true)
            },
            symphony.groove.artist.onUpdate.subscribe {
                updateArtistsState(true)
            },
            symphony.groove.album.onUpdate.subscribe {
                updateAlbumsState(true)
            },
            symphony.groove.albumArtist.onUpdate.subscribe {
                updateAlbumArtistsState(true)
            },
            symphony.groove.genre.onUpdate.subscribe {
                updateGenresState(true)
            },
            symphony.groove.playlist.onUpdate.subscribe {
                updatePlaylistsState(true)
            },
            symphony.groove.song.onUpdateEnd.subscribe {
                updateSongsState(false)
            },
            symphony.groove.artist.onUpdateEnd.subscribe {
                updateArtistsState(false)
            },
            symphony.groove.album.onUpdateEnd.subscribe {
                updateAlbumsState(false)
            },
            symphony.groove.albumArtist.onUpdateEnd.subscribe {
                updateAlbumArtistsState(false)
            },
            symphony.groove.genre.onUpdateEnd.subscribe {
                updateGenresState(false)
            },
            symphony.groove.playlist.onUpdateEnd.subscribe {
                updatePlaylistsState(false)
            },
        )
    }

    fun dispose() {
        subscribers.forEach { it() }
    }

    private fun updateAllStates() {
        updateSongsState(songsIsUpdating)
        updateArtistsState(artistsIsUpdating)
        updateAlbumsState(albumsIsUpdating)
        updateAlbumArtistsState(albumArtistsIsUpdating)
        updateGenresState(genresIsUpdating)
        updatePlaylistsState(playlistsIsUpdating)
    }

    private fun updateSongsState(intermediate: Boolean) {
        songsIsUpdating = symphony.groove.song.isUpdating
        songsCount = symphony.groove.song.count()
        if (intermediate) return
        songsMutable.swap(symphony.groove.song.getAll())
        songsExplorerId = System.currentTimeMillis()
    }

    private fun updateArtistsState(intermediate: Boolean) {
        artistsIsUpdating = symphony.groove.artist.isUpdating
        artistsCount = symphony.groove.artist.count()
        if (intermediate) return
        artistsMutable.swap(symphony.groove.artist.getAll())
    }

    private fun updateAlbumsState(intermediate: Boolean) {
        albumsIsUpdating = symphony.groove.album.isUpdating
        albumsCount = symphony.groove.album.count()
        if (intermediate) return
        albumsMutable.swap(symphony.groove.album.getAll())
    }

    private fun updateAlbumArtistsState(intermediate: Boolean) {
        albumArtistsIsUpdating = symphony.groove.albumArtist.isUpdating
        albumArtistsCount = symphony.groove.albumArtist.count()
        if (intermediate) return
        albumArtistsMutable.swap(symphony.groove.albumArtist.getAll())
    }

    private fun updateGenresState(intermediate: Boolean) {
        genresIsUpdating = symphony.groove.genre.isUpdating
        genresCount = symphony.groove.genre.count()
        if (intermediate) return
        genresMutable.swap(symphony.groove.genre.getAll())
    }

    private fun updatePlaylistsState(intermediate: Boolean) {
        playlistsIsUpdating = symphony.groove.playlist.isUpdating
        playlistsCount = symphony.groove.playlist.count()
        if (intermediate) return
        playlistsMutable.swap(symphony.groove.playlist.getAll())
    }
}
