package io.github.zyrouge.symphony.ui.view.home

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.Playlist
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.components.NewPlaylistDialog
import io.github.zyrouge.symphony.ui.components.PlaylistGrid
import io.github.zyrouge.symphony.ui.components.ScaffoldDialog
import io.github.zyrouge.symphony.ui.components.SubtleCaptionText
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import kotlinx.coroutines.launch

@Composable
fun PlaylistsView(context: ViewContext) {
    val coroutineScope = rememberCoroutineScope()
    val isUpdating by context.symphony.groove.playlist.isUpdating.collectAsState()
    val playlists by context.symphony.groove.playlist.all.collectAsState()
    val playlistsCount by context.symphony.groove.playlist.count.collectAsState()
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
                    Icons.Filled.Add,
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
                    Icons.Filled.ImportExport,
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

