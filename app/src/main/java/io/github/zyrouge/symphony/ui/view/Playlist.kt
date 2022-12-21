package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.components.*
import io.github.zyrouge.symphony.ui.helpers.ScreenOrientation
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.swap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistView(context: ViewContext, playlistId: String) {
    var playlist by remember {
        mutableStateOf(context.symphony.groove.playlist.getPlaylistWithId(playlistId))
    }
    val songs = remember {
        mutableStateListOf<Song>().apply {
            swap(context.symphony.groove.song.getSongsOfPlaylist(playlistId))
        }
    }
    var isViable by remember { mutableStateOf(playlist != null) }

    EventerEffect(context.symphony.groove.artist.onUpdate) {
        playlist = context.symphony.groove.playlist.getPlaylistWithId(playlistId)
        songs.swap(context.symphony.groove.song.getSongsOfPlaylist(playlistId))
        isViable = playlist != null
    }

    EventerEffect(context.symphony.groove.song.onUpdate) {
        songs.swap(context.symphony.groove.song.getSongsOfPlaylist(playlistId))
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { context.navController.popBackStack() }
                    ) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                title = {
                    TopAppBarMinimalTitle {
                        Text(
                            context.symphony.t.album
                                    + (playlist?.let { " - ${it.title}" } ?: "")
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
            )
        },
        content = { contentPadding ->
            Box(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
            ) {
                when {
                    isViable -> SongList(context, songs = songs)
                    else -> UnknownPlaylist(context, playlistId)
                }
            }
        },
        bottomBar = {
            NowPlayingBottomBar(context)
        }
    )
}

@Composable
private fun AlbumHero(context: ViewContext, album: Album) {
    val defaultHorizontalPadding = 20.dp
    BoxWithConstraints {
        AsyncImage(
            album.createArtworkImageRequest(context.symphony).build(),
            null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    when (ScreenOrientation.fromConfiguration(LocalConfiguration.current)) {
                        ScreenOrientation.PORTRAIT -> maxWidth.times(0.7f)
                        ScreenOrientation.LANDSCAPE -> maxWidth.times(0.25f)
                    }
                )
        )
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.Companion.verticalGradient(
                        0f to Color.Transparent,
                        1f to MaterialTheme.colorScheme.surface.copy(
                            alpha = 0.7f
                        )
                    )
                )
                .align(Alignment.BottomStart)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(
                modifier = Modifier
                    .padding(defaultHorizontalPadding, 32.dp, defaultHorizontalPadding, 12.dp)
                    .weight(1f)
            ) {
                Text(
                    album.name,
                    style = MaterialTheme.typography.headlineSmall
                        .copy(fontWeight = FontWeight.Bold)
                )
                album.artist?.let { artistName ->
                    Text(artistName)
                }
            }

            Box(modifier = Modifier.padding(4.dp)) {
                var showOptionsMenu by remember {
                    mutableStateOf(false)
                }
                IconButton(
                    onClick = {
                        showOptionsMenu = !showOptionsMenu
                    }
                ) {
                    Icon(Icons.Default.MoreVert, null)
                    AlbumDropdownMenu(
                        context,
                        album,
                        expanded = showOptionsMenu,
                        onDismissRequest = {
                            showOptionsMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun UnknownPlaylist(context: ViewContext, playlistId: String) {
    IconTextBody(
        icon = { modifier ->
            Icon(
                Icons.Default.QueueMusic,
                null,
                modifier = modifier
            )
        },
        content = {
            Text(context.symphony.t.unknownPlaylistX(playlistId))
        }
    )
}
