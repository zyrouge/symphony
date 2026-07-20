package io.github.zyrouge.symphony.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
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
import io.github.zyrouge.symphony.services.groove.entities.Artist
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.createGrooveArtworkImageRequests
import io.github.zyrouge.symphony.ui.view.ArtistViewRoute
import kotlinx.coroutines.launch

@Composable
fun ArtistTile(context: ViewContext, artist: Artist) {
    val artworkUris by context.symphony.groove.artist.getTop4ArtworkUriAsFlow(artist.id)
        .collectAsStateWithLifecycle(listOf())

    SquareGrooveTile(
        images = createGrooveArtworkImageRequests(context.symphony, artworkUris),
        options = { expanded, onDismissRequest ->
            ArtistDropdownMenu(
                context,
                artist,
                expanded = expanded,
                onDismissRequest = onDismissRequest,
            )
        },
        content = {
            Text(
                artist.name,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        onPlay = {
            context.symphony.groove.coroutineScope.launch {
                val songs = context.symphony.groove.artist.findSongsById(
                    artist.id,
                    context.symphony.settings.lastUsedArtistSongsSortBy.value,
                    context.symphony.settings.lastUsedArtistSongsSortReverse.value
                )
                context.symphony.radio.clear()
                context.symphony.radio.add(songs)
                context.symphony.radio.play()
            }
        },
        onClick = {
            context.navController.navigate(ArtistViewRoute(artist.name))
        }
    )
}

@Composable
fun ArtistDropdownMenu(
    context: ViewContext,
    artist: Artist,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
) {
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
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
                    val songs = context.symphony.groove.artist.findSongsById(
                        artist.id,
                        context.symphony.settings.lastUsedArtistSongsSortBy.value,
                        context.symphony.settings.lastUsedArtistSongsSortReverse.value
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
                    val songs = context.symphony.groove.artist.findSongsById(
                        artist.id,
                        context.symphony.settings.lastUsedArtistSongsSortBy.value,
                        context.symphony.settings.lastUsedArtistSongsSortReverse.value
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
    }

    if (showAddToPlaylistDialog) {
        val songs = remember {
            context.symphony.groove.artist.findSongsById(
                artist.id,
                context.symphony.settings.lastUsedArtistSongsSortBy.value,
                context.symphony.settings.lastUsedArtistSongsSortReverse.value
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
