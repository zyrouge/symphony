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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.mutate
import kotlinx.coroutines.launch

@Composable
fun AddToPlaylistDialog(
    context: ViewContext,
    songIds: List<Long>,
    onDismissRequest: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var showNewPlaylistDialog by remember { mutableStateOf(false) }
    val allPlaylistsIds by context.symphony.groove.playlist.all.collectAsState()
    val playlists by remember(allPlaylistsIds) {
        derivedStateOf {
            allPlaylistsIds
                .mapNotNull { context.symphony.groove.playlist.get(it) }
                .filter { it.isNotLocal() }
                .toMutableStateList()
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
                        GenericGrooveCard(
                            image = playlist
                                .createArtworkImageRequest(context.symphony)
                                .build(),
                            imageLabel = when {
                                songIds.size == 1 && playlist.songIds.contains(songIds[0]) -> ({
                                    Icon(
                                        Icons.Filled.Check,
                                        null,
                                        modifier = Modifier.size(12.dp),
                                    )
                                })

                                else -> null
                            },
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
                                    context.symphony.groove.playlist.update(
                                        playlist.id,
                                        playlist.songIds.mutate { addAll(songIds) },
                                    )
                                    onDismissRequest()
                                }
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
                showNewPlaylistDialog = false
                coroutineScope.launch {
                    context.symphony.groove.playlist.add(playlist)
                }
            },
            onDismissRequest = {
                showNewPlaylistDialog = false
            }
        )
    }
}
