package io.github.zyrouge.symphony.ui.view.home

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.Playlist
import io.github.zyrouge.symphony.ui.components.*
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import kotlinx.coroutines.launch

@Composable
fun PlaylistsView(context: ViewContext) {
    val isUpdating by context.symphony.groove.playlist.isUpdating.collectAsState()
    val playlists = context.symphony.groove.playlist.all
    val playlistsCount by context.symphony.groove.playlist.count.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var showPlaylistCreator by remember { mutableStateOf(false) }
    var showPlaylistPicker by remember { mutableStateOf(false) }

    LoaderScaffold(context, isLoading = isUpdating) {
        PlaylistGrid(
            context,
            playlistIds = playlists,
            playlistsCount = playlistsCount,
            leadingContent = {
                PlaylistControlBar(
                    context,
                    showPlaylistCreator = {
                        showPlaylistCreator = true
                    },
                    showPlaylistPicker = {
                        showPlaylistPicker = true
                    },
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        )
    }

    if (showPlaylistPicker) {
        val localPlaylists = remember {
            context.symphony.groove.playlist.queryAllLocalPlaylists()
        }

        SelectPlaylistDialog(
            context,
            playlists = localPlaylists,
            onSelected = { local ->
                showPlaylistPicker = false
                coroutineScope.launch {
                    context.symphony.groove.playlist.parseLocal(local)?.let { playlist ->
                        context.symphony.groove.playlist.add(playlist)
                    } ?: run {
                        Toast.makeText(
                            context.symphony.applicationContext,
                            context.symphony.t.InvalidM3UFile,
                            Toast.LENGTH_LONG,
                        )
                    }
                }
            },
            onDismissRequest = {
                showPlaylistPicker = false
            }
        )
    }

    if (showPlaylistCreator) {
        NewPlaylistDialog(
            context,
            onDone = { playlist ->
                showPlaylistCreator = false
                coroutineScope.launch {
                    context.symphony.groove.playlist.add(playlist)
                }
            },
            onDismissRequest = {
                showPlaylistCreator = false
            }
        )
    }
}

@Composable
private fun PlaylistControlBar(
    context: ViewContext,
    showPlaylistCreator: () -> Unit,
    showPlaylistPicker: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(20.dp, 0.dp),
    ) {
        ElevatedButton(
            modifier = Modifier.weight(1f),
            onClick = showPlaylistCreator,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Add,
                    null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(context.symphony.t.NewPlaylist)
            }
        }
        ElevatedButton(
            modifier = Modifier.weight(1f),
            onClick = showPlaylistPicker,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.ImportExport,
                    null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(context.symphony.t.ImportPlaylist)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectPlaylistDialog(
    context: ViewContext,
    playlists: List<Playlist.LocalExtended>,
    onSelected: (Playlist.LocalExtended) -> Unit,
    onDismissRequest: () -> Unit,
) {
    ScaffoldDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(context.symphony.t.ImportPlaylist)
        },
        content = {
            when {
                playlists.isEmpty() -> Box(modifier = Modifier.padding(0.dp, 12.dp)) {
                    SubtleCaptionText(context.symphony.t.NoLocalPlaylistsFound)
                }

                else -> LazyColumn(modifier = Modifier.padding(bottom = 4.dp)) {
                    items(playlists) { playlist ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            onClick = {
                                onSelected(playlist)
                            },
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp, 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Filled.Description,
                                    null,
                                    modifier = Modifier.size(32.dp),
                                )
                                Spacer(modifier = Modifier.width(20.dp))
                                Column {
                                    val path = playlist.path.split("/")
                                    Text(path.last())
                                    Text(
                                        path.subList(0, path.size - 1).joinToString("/"),
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
    )
}

@Composable
fun NewPlaylistDialog(
    context: ViewContext,
    onDone: (Playlist) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var input by remember { mutableStateOf("") }
    var showSongsPicker by remember { mutableStateOf(false) }
    val songIds = remember { mutableStateListOf<Long>() }
    val songIdsImmutable = songIds.toList()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(LocalContext.current) {
        focusRequester.requestFocus()
    }

    ScaffoldDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(context.symphony.t.NewPlaylist)
        },
        content = {
            Box(
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = DividerDefaults.color,
                    ),
                    value = input,
                    onValueChange = {
                        input = it
                    }
                )
            }
        },
        actions = {
            TextButton(
                onClick = {
                    showSongsPicker = true
                }
            ) {
                Text(context.symphony.t.AddSongs + " (${songIds.size})")
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                enabled = input.isNotBlank(),
                onClick = {
                    val playlist = context.symphony.groove.playlist.create(
                        title = input,
                        songIds = songIds.toList(),
                    )
                    onDone(playlist)
                }
            ) {
                Text(context.symphony.t.Done)
            }
        },
    )

    if (showSongsPicker) {
        PlaylistManageSongsDialog(
            context,
            selectedSongIds = songIdsImmutable,
            onDone = {
                showSongsPicker = false
                songIds.clear()
                songIds.addAll(it)
            }
        )
    }
}
