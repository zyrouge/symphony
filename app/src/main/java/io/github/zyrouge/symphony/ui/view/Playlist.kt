package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.zyrouge.symphony.ui.components.*
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.ThemeColors
import io.github.zyrouge.symphony.utils.mutate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistView(context: ViewContext, playlistId: String) {
    val coroutineScope = rememberCoroutineScope()
    val allPlaylistIds = context.symphony.groove.playlist.all
    val playlist by remember(allPlaylistIds) {
        derivedStateOf { context.symphony.groove.playlist.get(playlistId) }
    }
    val songIds by remember {
        derivedStateOf { playlist?.songIds ?: emptyList() }
    }
    val isViable by remember {
        derivedStateOf { allPlaylistIds.contains(playlistId) }
    }
    var showOptionsMenu by remember { mutableStateOf(false) }
    val isFavoritesPlaylist by remember {
        derivedStateOf {
            playlist?.let {
                context.symphony.groove.playlist.isFavoritesPlaylist(it)
            } ?: false
        }
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
                            context.symphony.t.Playlist
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
                        songIds = songIds,
                        type = SongListType.Playlist,
                        disableHeartIcon = isFavoritesPlaylist,
                        trailingOptionsContent = { _, song, onDismissRequest ->
                            playlist?.takeIf { it.isNotLocal() }?.let {
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.DeleteForever,
                                            null,
                                            tint = ThemeColors.Red,
                                        )
                                    },
                                    text = {
                                        Text(context.symphony.t.RemoveFromPlaylist)
                                    },
                                    onClick = {
                                        onDismissRequest()
                                        coroutineScope.launch {
                                            context.symphony.groove.playlist.update(
                                                it.id,
                                                it.songIds.mutate {
                                                    remove(song.id)
                                                },
                                            )
                                        }
                                    }
                                )
                            }
                        },
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
            Text(context.symphony.t.UnknownPlaylistX(playlistId))
        }
    )
}
