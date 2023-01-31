package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.services.radio.RadioEvents
import io.github.zyrouge.symphony.ui.helpers.RoutesBuilder
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongCard(
    context: ViewContext,
    song: Song,
    highlighted: Boolean = false,
    autoHighlight: Boolean = true,
    leading: @Composable () -> Unit = {},
    thumbnailLabel: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    var isCurrentPlaying by remember {
        mutableStateOf(autoHighlight && song.id == context.symphony.radio.queue.currentPlayingSong?.id)
    }

    if (autoHighlight) {
        EventerEffect(context.symphony.radio.onUpdate) {
            if (it == RadioEvents.StartPlaying || it == RadioEvents.StopPlaying) {
                isCurrentPlaying = song.id == context.symphony.radio.queue.currentPlayingSong?.id
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        onClick = onClick
    ) {
        Box(modifier = Modifier.padding(12.dp, 12.dp, 4.dp, 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                leading()
                Box {
                    AsyncImage(
                        song.createArtworkImageRequest(context.symphony).build(),
                        null,
                        modifier = Modifier
                            .size(45.dp)
                            .clip(RoundedCornerShape(10.dp)),
                    )
                    thumbnailLabel?.let { it ->
                        Box(
                            modifier = Modifier
                                .offset(y = 8.dp)
                                .align(Alignment.BottomCenter)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(3.dp, 0.dp)
                            ) {
                                ProvideTextStyle(
                                    MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                ) { it() }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        song.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (highlighted || isCurrentPlaying) MaterialTheme.colorScheme.primary
                            else LocalTextStyle.current.color
                        )
                    )
                    song.artistName?.let { artistName ->
                        Text(
                            artistName,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(modifier = Modifier.width(15.dp))

                var showOptionsMenu by remember { mutableStateOf(false) }
                IconButton(
                    onClick = { showOptionsMenu = !showOptionsMenu }
                ) {
                    Icon(Icons.Default.MoreVert, null)
                    SongDropdownMenu(
                        context,
                        song,
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
fun SongDropdownMenu(
    context: ViewContext,
    song: Song,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
) {
    var showInfoDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Default.PlaylistPlay, null)
            },
            text = {
                Text(context.symphony.t.playNext)
            },
            onClick = {
                onDismissRequest()
                context.symphony.radio.queue.add(
                    song,
                    context.symphony.radio.queue.currentSongIndex + 1
                )
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Default.PlaylistPlay, null)
            },
            text = {
                Text(context.symphony.t.addToQueue)
            },
            onClick = {
                onDismissRequest()
                context.symphony.radio.queue.add(song)
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Default.PlaylistAdd, null)
            },
            text = {
                Text(context.symphony.t.addToPlaylist)
            },
            onClick = {
                onDismissRequest()
                showAddToPlaylistDialog = true
            }
        )
        song.artistName?.let { artistName ->
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Default.Person, null)
                },
                text = {
                    Text(context.symphony.t.viewArtist)
                },
                onClick = {
                    onDismissRequest()
                    context.navController.navigate(
                        RoutesBuilder.buildArtistRoute(artistName)
                    )
                }
            )
        }
        song.additional.albumArtist?.let { albumArtist ->
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Default.Person, null)
                },
                text = {
                    Text(context.symphony.t.viewAlbumArtist)
                },
                onClick = {
                    onDismissRequest()
                    context.navController.navigate(
                        RoutesBuilder.buildArtistRoute(albumArtist)
                    )
                }
            )
        }
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Default.Album, null)
            },
            text = {
                Text(context.symphony.t.viewAlbum)
            },
            onClick = {
                onDismissRequest()
                context.navController.navigate(
                    RoutesBuilder.buildAlbumRoute(song.albumId)
                )
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Default.Info, null)
            },
            text = {
                Text(context.symphony.t.details)
            },
            onClick = {
                onDismissRequest()
                showInfoDialog = true
            }
        )
    }

    if (showInfoDialog) {
        SongInformationDialog(
            context,
            song = song,
            onDismissRequest = {
                showInfoDialog = false
            }
        )
    }

    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            context,
            song = song,
            onDismissRequest = {
                showAddToPlaylistDialog = false
            }
        )
    }
}

@Composable
private fun AddToPlaylistDialog(
    context: ViewContext,
    song: Song,
    onDismissRequest: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val playlists = remember {
        context.symphony.groove.playlist.getAll().filter {
            it.isNotLocal()
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(modifier = Modifier.clip(RoundedCornerShape(8.dp))) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(20.dp, 0.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        context.symphony.t.addToPlaylist,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Divider()

                when {
                    playlists.isEmpty() -> Text(
                        context.symphony.t.noLocalPlaylistsFound,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 20.dp),
                    )
                    else -> LazyColumn(modifier = Modifier.padding(bottom = 4.dp)) {
                        items(playlists) { playlist ->
                            GenericGrooveCard(
                                image = playlist.createArtworkImageRequest(context.symphony)
                                    .build(),
                                title = {
                                    Text(playlist.title)
                                },
                                options = { expanded, onDismissRequest ->
                                    PlaylistDropdownMenu(
                                        context,
                                        playlist,
                                        expanded = expanded,
                                        onDismissRequest = onDismissRequest,
                                    )
                                },
                                onClick = {
                                    coroutineScope.launch {
                                        context.symphony.groove.playlist.updatePlaylistSongs(
                                            playlist = playlist,
                                            songs = playlist.songs.toMutableList().apply {
                                                add(song.id)
                                            }
                                        )
                                        onDismissRequest()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
