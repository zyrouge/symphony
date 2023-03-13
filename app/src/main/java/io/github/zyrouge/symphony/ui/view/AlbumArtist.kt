package io.github.zyrouge.symphony.ui.view

import androidx.compose.runtime.*
import io.github.zyrouge.symphony.ui.components.EventerEffect
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.swap

@Composable
fun AlbumArtistView(context: ViewContext, artistName: String) {
    var artist by remember {
        mutableStateOf(context.symphony.groove.artist.getArtistFromName(artistName))
    }
    val songs = remember {
        context.symphony.groove.song.getSongsOfAlbumArtist(artistName).toMutableStateList()
    }
    val albums = remember {
        context.symphony.groove.album.getAlbumsOfAlbumArtist(artistName).toMutableStateList()
    }
    var isViable by remember { mutableStateOf(artist != null) }

    val onAlbumArtistUpdate = {
        artist = context.symphony.groove.artist.getArtistFromName(artistName)
        songs.swap(context.symphony.groove.song.getSongsOfArtist(artistName))
        isViable = artist != null
    }

    EventerEffect(context.symphony.groove.artist.onUpdate) { onAlbumArtistUpdate() }
    EventerEffect(context.symphony.groove.albumArtist.onUpdate) { onAlbumArtistUpdate() }

    EventerEffect(context.symphony.groove.song.onUpdate) {
        songs.swap(context.symphony.groove.song.getSongsOfArtist(artistName))
    }

    EventerEffect(context.symphony.groove.album.onUpdate) {
        albums.swap(context.symphony.groove.album.getAlbumsOfArtist(artistName))
    }

    ArtistViewScaffold(
        context,
        isViable = isViable,
        artistName = artistName,
        artist = artist,
        songs = songs,
        albums = albums,
        titlePrefix = context.symphony.t.AlbumArtist,
    )
}
