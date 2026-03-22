package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.zyrouge.symphony.services.groove.entities.Song
import io.github.zyrouge.symphony.services.groove.repositories.PlaylistRepository
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.createGrooveArtworkImageRequests
import kotlinx.coroutines.launch

@Composable
fun AddToPlaylistDialog(
    context: ViewContext,
    songs: List<Song>,
    onDismissRequest: () -> Unit,
) {
    var showNewPlaylistDialog by remember { mutableStateOf(false) }
    val sortBy by context.symphony.settings.lastUsedPlaylistsSortBy.flow.collectAsStateWithLifecycle()
    val sortReverse by context.symphony.settings.lastUsedPlaylistsSortReverse.flow.collectAsStateWithLifecycle()
    val allPlaylists by context.symphony.groove.playlist.valuesAsFlow(sortBy, sortReverse)
        .collectAsStateWithLifecycle(listOf())
    val playlists by remember(allPlaylists) {
        derivedStateOf {
            allPlaylists.filter { !it.entity.isLocal }
        }
    }

    ScaffoldDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(context.symphony.t.AddToPlaylist)
        },
        content = {
            when {
                playlists.isEmpty() -> SubtleCaptionText(context.symphony.t.NoInAppPlaylistsFound)
                else -> LazyColumn(modifier = Modifier.padding(bottom = 4.dp)) {
                    items(playlists) { playlist ->
                        val containsSong by remember(songs) {
                            derivedStateOf {
                                val found = when {
                                    songs.size == 1 -> context.symphony.groove.playlist.findSongById(
                                        playlist.entity.id,
                                        songs[0].id
                                    )

                                    else -> null
                                }
                                found != null
                            }
                        }
                        val artworkUris by context.symphony.groove.playlist
                            .getTop4ArtworkUriAsFlow(playlist.entity.id)
                            .collectAsStateWithLifecycle(listOf())

                        GenericGrooveCard(
                            images = createGrooveArtworkImageRequests(
                                context.symphony,
                                artworkUris
                            ),
                            imageLabel = when {
                                containsSong -> ({
                                    Icon(
                                        Icons.Filled.Check,
                                        null,
                                        modifier = Modifier.size(12.dp),
                                    )
                                })

                                else -> null
                            },
                            title = {
                                Text(playlist.entity.title)
                            },
                            options = { expanded, onDismissRequest ->
                                PlaylistDropdownMenu(
                                    context,
                                    playlist.entity,
                                    expanded = expanded,
                                    onDismissRequest = onDismissRequest,
                                )
                            },
                            onClick = {
                                context.symphony.groove.coroutineScope.launch {
                                    context.symphony.groove.playlist.addSongs(
                                        playlist.entity.id,
                                        songs,
                                        PlaylistRepository.AddPosition.AfterTail,
                                    )
                                }
                                onDismissRequest()
                            }
                        )
                    }
                }
            }
        },
        removeActionsVerticalPadding = true,
        actions = {
            TextButton(
                modifier = Modifier.offset(y = (-8).dp),
                onClick = {
                    showNewPlaylistDialog = !showNewPlaylistDialog
                }
            ) {
                Text(context.symphony.t.NewPlaylist)
            }
            Spacer(modifier = Modifier.weight(1f))
        },
    )

    if (showNewPlaylistDialog) {
        NewPlaylistDialog(
            context = context,
            onDone = { playlist ->
                context.symphony.groove.coroutineScope.launch {
                    context.symphony.groove.playlist.addSongs(playlist.id, songs)
                }
                showNewPlaylistDialog = false
            },
            onDismissRequest = {
                showNewPlaylistDialog = false
            }
        )
    }
}
