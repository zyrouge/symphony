package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.home.NewPlaylistDialog
import io.github.zyrouge.symphony.utils.mutate
import kotlinx.coroutines.launch

@Composable
fun AddToPlaylistDialog(
    context: ViewContext,
    songs: List<Long>,
    onDismissRequest: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var showNewPlaylistDialog by remember { mutableStateOf(false) }
    val playlists = remember {
        context.symphony.groove.playlist.values()
            .filter { it.isNotLocal() }
            .toMutableStateList()
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
                                        songs = playlist.songs.mutate { addAll(songs) },
                                    )
                                    onDismissRequest()
                                }
                            }
                        )
                    }
                }
            }
        },
        removeActionsHorizontalPadding = true,
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
                showNewPlaylistDialog = false
                playlists.add(playlist)
            },
            onDismissRequest = {
                showNewPlaylistDialog = false
            }
        )
    }
}
