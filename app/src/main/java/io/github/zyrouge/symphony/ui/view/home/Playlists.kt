package io.github.zyrouge.symphony.ui.view.home

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.MediaExposer
import io.github.zyrouge.symphony.services.groove.Playlist
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.components.NewPlaylistDialog
import io.github.zyrouge.symphony.ui.components.PlaylistGrid
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.Logger
import kotlinx.coroutines.launch

@Composable
fun PlaylistsView(context: ViewContext) {
    val coroutineScope = rememberCoroutineScope()
    val isUpdating by context.symphony.groove.playlist.isUpdating.collectAsState()
    val playlists by context.symphony.groove.playlist.all.collectAsState()
    val playlistsCount by context.symphony.groove.playlist.count.collectAsState()
    var showPlaylistCreator by remember { mutableStateOf(false) }

    val openPlaylistLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(MediaExposer.MIMETYPE_M3U8)
    ) { uri ->
        uri?.let { _ ->
            try {
                val playlist = Playlist.parse(context.symphony, uri)
                context.symphony.groove.playlist.add(playlist)
            } catch (err: Exception) {
                Logger.error("PlaylistTile", "import failed (activity result)", err)
                Toast.makeText(
                    context.symphony.applicationContext,
                    context.symphony.t.InvalidM3UFile,
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }

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
                        openPlaylistLauncher.launch(null)
                    },
                )
                Spacer(modifier = Modifier.height(4.dp))
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
