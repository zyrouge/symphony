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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.zyrouge.symphony.services.groove.entities.Album
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.createGrooveArtworkImageRequests
import io.github.zyrouge.symphony.ui.view.AlbumViewRoute
import io.github.zyrouge.symphony.ui.view.ArtistViewRoute
import kotlinx.coroutines.launch

@Composable
fun AlbumTile(context: ViewContext, album: Album) {
    val artworkUris by context.symphony.groove.album.getTop4ArtworkUriAsFlow(album.id)
        .collectAsStateWithLifecycle(listOf())
    val artists by context.symphony.groove.album.findArtistsOfIdAsFlow(album.id)
        .collectAsStateWithLifecycle(listOf())

    SquareGrooveTile(
        images = createGrooveArtworkImageRequests(context.symphony, artworkUris),
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
            if (artists.isNotEmpty()) {
                Text(
                    artists.joinToString { it.entity.name },
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        onPlay = {
            context.symphony.groove.coroutineScope.launch {
                val songs = context.symphony.groove.album.findSongsById(
                    album.id,
                    context.symphony.settings.lastUsedAlbumSongsSortBy.value,
                    context.symphony.settings.lastUsedAlbumSongsSortReverse.value
                )
                context.symphony.radio.clear()
                context.symphony.radio.add(songs)
                context.symphony.radio.play()
            }
        },
        onClick = {
            context.navController.navigate(AlbumViewRoute(album.id))
        }
    )
}

@Composable
fun AlbumDropdownMenu(
    context: ViewContext,
    album: Album,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
) {
    val artists by context.symphony.groove.album.findArtistsOfIdAsFlow(album.id)
        .collectAsStateWithLifecycle(listOf())
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
                context.symphony.groove.coroutineScope.launch {
                    val songs = context.symphony.groove.album.findSongsById(
                        album.id,
                        context.symphony.settings.lastUsedAlbumSongsSortBy.value,
                        context.symphony.settings.lastUsedAlbumSongsSortReverse.value
                    )
                    context.symphony.radio.clear()
                    context.symphony.radio.add(songs)
                    context.symphony.radio.setShuffleMode(true)
                    context.symphony.radio.play()
                }
                onDismissRequest()
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
                // TODO
                onDismissRequest()
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
                context.symphony.groove.coroutineScope.launch {
                    val songs = context.symphony.groove.album.findSongsById(
                        album.id,
                        context.symphony.settings.lastUsedAlbumSongsSortBy.value,
                        context.symphony.settings.lastUsedAlbumSongsSortReverse.value
                    )
                    context.symphony.radio.add(songs)
                    context.symphony.radio.play()
                }
                onDismissRequest()
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
        artists.forEach {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Filled.Person, null)
                },
                text = {
                    Text("${context.symphony.t.ViewArtist}: ${it.entity.name}")
                },
                onClick = {
                    onDismissRequest()
                    context.navController.navigate(ArtistViewRoute(it.entity.name))
                }
            )
        }
    }

    if (showAddToPlaylistDialog) {
        val songs = remember {
            context.symphony.groove.album.findSongsById(
                album.id,
                context.symphony.settings.lastUsedAlbumSongsSortBy.value,
                context.symphony.settings.lastUsedAlbumSongsSortReverse.value
            )
        }

        AddToPlaylistDialog(
            context,
            songs = songs,
            onDismissRequest = {
                showAddToPlaylistDialog = false
            }
        )
    }
}
