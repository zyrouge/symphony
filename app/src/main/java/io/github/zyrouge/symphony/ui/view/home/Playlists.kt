package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.zyrouge.symphony.services.groove.Playlist
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.components.PlaylistGrid
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import kotlinx.coroutines.launch

@Composable
fun PlaylistsView(context: ViewContext, data: HomeViewData) {
    val coroutineScope = rememberCoroutineScope()
    var showPlaylistCreator by remember { mutableStateOf(false) }
    var showPlaylistPicker by remember { mutableStateOf(false) }
    when {
        data.playlists.isNotEmpty() -> {
            PlaylistGrid(
                context,
                data.playlists,
                isLoading = data.playlistsIsUpdating,
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
        else -> Column {
            PlaylistControlBar(
                context,
                showPlaylistCreator = {
                    showPlaylistCreator = true
                },
                showPlaylistPicker = {
                    showPlaylistPicker = true
                },
            )
            IconTextBody(
                icon = { modifier ->
                    Icon(
                        Icons.Default.QueueMusic,
                        null,
                        modifier = modifier,
                    )
                },
                content = {
                    Text(context.symphony.t.damnThisIsSoEmpty)
                }
            )
        }
    }

    if (showPlaylistPicker) {
        Dialog(
            onDismissRequest = {
                showPlaylistPicker = false
            }
        ) {
            SelectPlaylistDialog(
                context,
                playlists = context.symphony.groove.playlist.queryAllLocalPlaylists(),
                onSelect = { playlist ->
                    showPlaylistPicker = false
                    coroutineScope.launch {
                        context.symphony.groove.playlist.addLocalPlaylist(playlist)
                    }
                }
            )
        }
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
                Text(context.symphony.t.newPlaylist)
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
                Text(context.symphony.t.importPlaylist)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectPlaylistDialog(
    context: ViewContext,
    playlists: List<Playlist.Local>,
    onSelect: (Playlist.Local) -> Unit,
) {
    Surface(modifier = Modifier.clip(RoundedCornerShape(8.dp))) {
        LazyColumn(
            modifier = Modifier.padding(
                bottom = if (playlists.isNotEmpty()) 4.dp else 0.dp
            )
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(20.dp, 0.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        context.symphony.t.importPlaylist,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
            }
            if (playlists.isEmpty()) {
                item {
                    Text(
                        context.symphony.t.noLocalPlaylistsFound,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 20.dp),
                    )
                }
            }
            items(playlists) { playlist ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    onClick = { onSelect(playlist) },
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
}
