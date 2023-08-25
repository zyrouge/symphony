package io.github.zyrouge.symphony.ui.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.zyrouge.symphony.services.groove.Playlist
import io.github.zyrouge.symphony.services.parsers.M3U
import io.github.zyrouge.symphony.ui.helpers.RoutesBuilder
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.ThemeColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistTile(context: ViewContext, playlist: Playlist) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        onClick = {
            context.navController.navigate(RoutesBuilder.buildPlaylistRoute(playlist.id))
        }
    ) {
        Box(modifier = Modifier.padding(12.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box {
                    AsyncImage(
                        playlist.createArtworkImageRequest(context.symphony).build(),
                        null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp)),
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 4.dp)
                    ) {
                        var showOptionsMenu by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = { showOptionsMenu = !showOptionsMenu }
                        ) {
                            Icon(Icons.Default.MoreVert, null)
                            PlaylistDropdownMenu(
                                context,
                                playlist,
                                expanded = showOptionsMenu,
                                onDismissRequest = {
                                    showOptionsMenu = false
                                }
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    ) {
                        IconButton(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(12.dp)
                                )
                                .then(Modifier.size(36.dp)),
                            onClick = {
                                context.symphony.radio.shorty.playQueue(playlist.songIds)
                            }
                        ) {
                            Icon(Icons.Default.PlayArrow, null)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    playlist.title,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun PlaylistDropdownMenu(
    context: ViewContext,
    playlist: Playlist,
    expanded: Boolean,
    onDelete: (() -> Unit)? = {},
    onDismissRequest: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val savePlaylistLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(M3U.mimeType)
    ) { uri ->
        uri?.let { _ ->
            try {
                context.symphony.groove.playlist.savePlaylist(playlist, uri)
                Toast.makeText(
                    context.activity,
                    context.symphony.t.ExportedX(playlist.basename),
                    Toast.LENGTH_SHORT,
                ).show()
            } catch (err: Exception) {
                Toast.makeText(
                    context.activity,
                    context.symphony.t.ExportFailedX(
                        err.localizedMessage ?: err.toString()
                    ),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    var showSongsPicker by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
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
                Text(context.symphony.t.ShufflePlay)
            },
            onClick = {
                onDismissRequest()
                context.symphony.radio.shorty.playQueue(
                    playlist.songIds,
                    shuffle = true,
                )
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Default.PlaylistPlay, null)
            },
            text = {
                Text(context.symphony.t.PlayNext)
            },
            onClick = {
                onDismissRequest()
                context.symphony.radio.queue.add(
                    playlist.songIds,
                    context.symphony.radio.queue.currentSongIndex + 1
                )
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Default.PlaylistAdd, null)
            },
            text = {
                Text(context.symphony.t.AddToPlaylist)
            },
            onClick = {
                onDismissRequest()
                showAddToPlaylistDialog = true
            }
        )
        if (playlist.isNotLocal()) {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Default.PlaylistAdd, null)
                },
                text = {
                    Text(context.symphony.t.ManageSongs)
                },
                onClick = {
                    onDismissRequest()
                    showSongsPicker = true
                }
            )
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Default.Save, null)
                },
                text = {
                    Text(context.symphony.t.Export)
                },
                onClick = {
                    onDismissRequest()
                    try {
                        savePlaylistLauncher.launch(playlist.basename)
                    } catch (err: Exception) {
                        Toast.makeText(
                            context.activity,
                            context.symphony.t.ExportFailedX(
                                err.localizedMessage ?: err.toString()
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Default.Info, null)
            },
            text = {
                Text(context.symphony.t.Details)
            },
            onClick = {
                onDismissRequest()
                showInfoDialog = true
            }
        )
        if (!context.symphony.groove.playlist.isBuiltInPlaylist(playlist)) {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        Icons.Default.DeleteForever,
                        null,
                        tint = ThemeColors.Red,
                    )
                },
                text = {
                    Text(context.symphony.t.Delete)
                },
                onClick = {
                    onDismissRequest()
                    showDeleteDialog = true
                }
            )
        }
    }

    if (showInfoDialog) {
        PlaylistInformationDialog(
            context,
            playlist = playlist,
            onDismissRequest = {
                showInfoDialog = false
            }
        )
    }

    if (showSongsPicker) {
        PlaylistManageSongsDialog(
            context,
            selectedSongIds = playlist.songIds,
            onDone = {
                coroutineScope.launch {
                    context.symphony.groove.playlist.update(playlist.id, it)
                    showSongsPicker = false
                }
            }
        )
    }

    if (showDeleteDialog) {
        ConfirmationDialog(
            context,
            title = {
                Text(context.symphony.t.DeletePlaylist)
            },
            description = {
                Text(context.symphony.t.AreYouSureThatYouWantToDeleteThisPlaylist)
            },
            onResult = { result ->
                coroutineScope.launch {
                    showDeleteDialog = false
                    if (result) {
                        onDelete?.invoke()
                        context.symphony.groove.playlist.delete(playlist.id)
                    }
                }
            }
        )
    }

    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            context,
            songIds = playlist.songIds,
            onDismissRequest = {
                showAddToPlaylistDialog = false
            }
        )
    }
}
