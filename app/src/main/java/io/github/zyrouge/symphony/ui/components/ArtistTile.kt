package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.PlaylistPlay
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
import io.github.zyrouge.symphony.services.groove.Artist
import io.github.zyrouge.symphony.ui.helpers.RoutesBuilder
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistTile(context: ViewContext, artist: Artist) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        onClick = {
            context.navController.navigate(RoutesBuilder.buildArtistRoute(artist.artistName))
        }
    ) {
        Box(modifier = Modifier.padding(12.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box {
                    Image(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp)),
                        bitmap = artist.getArtwork(context.symphony, 250)
                            .asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop
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
                            ArtistDropdownMenu(
                                context,
                                artist,
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
                                    context.symphony.groove.song.getSongsOfArtist(artist.artistName)
                                context.symphony.radio.shorty.playQueue(songs)
                            }
                        ) {
                            Icon(Icons.Default.PlayArrow, null)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    artist.artistName,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun ArtistDropdownMenu(
    context: ViewContext,
    artist: Artist,
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
                Text(context.symphony.t.shufflePlay)
            },
            onClick = {
                onDismissRequest()
                context.symphony.radio.shorty.playQueue(
                    context.symphony.groove.song.getSongsOfArtist(artist.artistName),
                    shuffle = true,
                )
            }
        )
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
                    context.symphony.groove.song.getSongsOfArtist(artist.artistName),
                    context.symphony.radio.queue.currentSongIndex + 1
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
                context.symphony.radio.queue.add(
                    context.symphony.groove.song.getSongsOfArtist(artist.artistName)
                )
            }
        )
    }
}
