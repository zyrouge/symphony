package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.PlayerEvent
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.helpers.RoutesBuilder
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongCard(
    context: ViewContext,
    song: Song,
    highlighted: Boolean = false,
    autoHighlight: Boolean = true,
    leading: @Composable () -> Unit = {},
    onClick: () -> Unit
) {
    var isCurrentPlaying by remember {
        mutableStateOf(false)
    }
    isCurrentPlaying = autoHighlight && song.id == context.symphony.player.currentPlayingSong?.id

    if (autoHighlight) {
        EventerEffect(context.symphony.player.onUpdate) {
            if (it == PlayerEvent.StartPlaying || it == PlayerEvent.StopPlaying) {
                isCurrentPlaying = song.id == context.symphony.player.currentPlayingSong?.id
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
                Image(
                    song.getArtwork(context.symphony).asImageBitmap(),
                    null,
                    modifier = Modifier
                        .size(45.dp)
                        .clip(RoundedCornerShape(10.dp)),
                )
                Spacer(modifier = Modifier.width(15.dp))
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
    onDismissRequest: () -> Unit
) {
    var showInfoDialog by remember { mutableStateOf(false) }

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
                context.symphony.player.addToQueue(
                    song,
                    context.symphony.player.currentSongIndex + 1
                )
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Default.PlaylistAdd, null)
            },
            text = {
                Text(context.symphony.t.addToQueue)
            },
            onClick = {
                onDismissRequest()
                context.symphony.player.addToQueue(song)
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
}
