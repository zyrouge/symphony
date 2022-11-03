package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.ui.helpers.RoutesBuilder
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumTile(context: ViewContext, album: Album) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        onClick = {
            context.navController.navigate(RoutesBuilder.buildAlbumRoute(album.albumId))
        }
    ) {
        Box(
            modifier = Modifier
                .padding(12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BoxWithConstraints {
                    Image(
                        modifier = Modifier
                            .size(maxWidth)
                            .clip(RoundedCornerShape(10.dp)),
                        bitmap = album.getArtwork(context.symphony, 250)
                            .asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                    Box(modifier = Modifier.align(Alignment.TopEnd)) {
                        var showOptionsMenu by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = { showOptionsMenu = !showOptionsMenu }
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
                                val songs =
                                    context.symphony.groove.song.getSongsOfAlbum(album.albumId)
                                context.symphony.player.stop()
                                if (songs.isNotEmpty()) {
                                    context.symphony.player.addToQueue(songs)
                                }
                            }
                        ) {
                            Icon(Icons.Default.PlayArrow, null)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    album.albumName,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
                album.artistName?.let { artistName ->
                    Text(
                        artistName,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
fun AlbumDropdownMenu(
    context: ViewContext,
    album: Album,
    expanded: Boolean,
    onDismissRequest: () -> Unit
) {
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
                    context.symphony.groove.song.getSongsOfAlbum(album.albumId),
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
                context.symphony.player.addToQueue(
                    context.symphony.groove.song.getSongsOfAlbum(album.albumId)
                )
            }
        )
        album.artistName?.let { artistName ->
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
    }
}
