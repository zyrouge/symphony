package io.github.zyrouge.symphony.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
                context.symphony.groove.album.getSongIds(album.id)
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
                Icon(Icons.AutoMirrored.Filled.PlaylistPlay, null)
            },
            text = {
                Text(context.symphony.t.ShufflePlay)
            },
            onClick = {
                onDismissRequest()
                context.symphony.radio.shorty.playQueue(
                    context.symphony.groove.album.getSongIds(album.id),
                    shuffle = true,
                )
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.AutoMirrored.Filled.PlaylistPlay, null)
            },
            text = {
                Text(context.symphony.t.PlayNext)
            },
            onClick = {
                onDismissRequest()
                context.symphony.radio.queue.add(
                    context.symphony.groove.album.getSongIds(album.id),
                    context.symphony.radio.queue.currentSongIndex + 1
                )
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.AutoMirrored.Filled.PlaylistPlay, null)
            },
            text = {
                Text(context.symphony.t.AddToQueue)
            },
            onClick = {
                onDismissRequest()
                context.symphony.radio.queue.add(
                    context.symphony.groove.album.getSongIds(album.id)
                )
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null)
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
                    Icon(Icons.Filled.Person, null)
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
            songIds = context.symphony.groove.album.getSongIds(album.id),
            onDismissRequest = {
                showAddToPlaylistDialog = false
            }
        )
    }
}
