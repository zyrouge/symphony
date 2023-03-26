package io.github.zyrouge.symphony.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.ui.helpers.RoutesBuilder
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun AlbumTile(context: ViewContext, album: Album) {
    SquareGrooveTile(
        image = album.createArtworkImageRequest(context.symphony).build(),
        options = { expanded, onDismissRequest ->
            AlbumDropdownMenu(
                context,
                album,
                expanded = expanded,
                onDismissRequest = onDismissRequest,
            )
        },
        content = {
            Text(
                album.name,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            album.artist?.let { artistName ->
                Text(
                    artistName,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        onPlay = {
            context.symphony.radio.shorty.playQueue(
                context.symphony.groove.album.getSongIdsOfAlbumId(album.id)
            )
        },
        onClick = {
            context.navController.navigate(RoutesBuilder.buildAlbumRoute(album.id))
        }
    )
}

@Composable
fun AlbumDropdownMenu(
    context: ViewContext,
    album: Album,
    expanded: Boolean,
    onDismissRequest: () -> Unit
) {
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
                    context.symphony.groove.album.getSongIdsOfAlbumId(album.id),
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
                    context.symphony.groove.album.getSongIdsOfAlbumId(album.id),
                    context.symphony.radio.queue.currentSongIndex + 1
                )
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Default.PlaylistPlay, null)
            },
            text = {
                Text(context.symphony.t.AddToQueue)
            },
            onClick = {
                onDismissRequest()
                context.symphony.radio.queue.add(
                    context.symphony.groove.album.getSongIdsOfAlbumId(album.id)
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
        album.artist?.let { artistName ->
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Default.Person, null)
                },
                text = {
                    Text(context.symphony.t.ViewArtist)
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

    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            context,
            songs = context.symphony.groove.album.getSongIdsOfAlbumId(album.id),
            onDismissRequest = {
                showAddToPlaylistDialog = false
            }
        )
    }
}
