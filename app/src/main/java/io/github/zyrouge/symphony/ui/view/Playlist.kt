package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.zyrouge.symphony.ui.components.*
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistView(context: ViewContext, playlistId: String) {
    var playlist by remember {
        mutableStateOf(context.symphony.groove.playlist.getPlaylistWithId(playlistId))
    }
    var songs by remember {
        mutableStateOf(context.symphony.groove.song.getSongsOfPlaylist(playlistId))
    }
    var isViable by remember { mutableStateOf(playlist != null) }
    var showOptionsMenu by remember { mutableStateOf(false) }

    EventerEffect(context.symphony.groove.playlist.onUpdate) {
        playlist = context.symphony.groove.playlist.getPlaylistWithId(playlistId)
        songs = context.symphony.groove.song.getSongsOfPlaylist(playlistId)
        isViable = playlist != null
    }

    EventerEffect(context.symphony.groove.song.onUpdate) {
        songs = context.symphony.groove.song.getSongsOfPlaylist(playlistId)
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
                            context.symphony.t.playlist
                                    + (playlist?.let { " - ${it.title}" } ?: "")
                        )
                    }
                },
                actions = {
                    if (isViable) {
                        IconButton(
                            onClick = {
                                showOptionsMenu = true
                            }
                        ) {
                            Icon(Icons.Default.MoreVert, null)
                            PlaylistDropdownMenu(
                                context,
                                playlist!!,
                                expanded = showOptionsMenu,
                                onDelete = {
                                    context.navController.popBackStack()
                                },
                                onDismissRequest = {
                                    showOptionsMenu = false
                                }
                            )
                        }
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
                    isViable -> SongList(
                        context,
                        songs = songs,
                        type = SongListType.Playlist,
                    )
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
