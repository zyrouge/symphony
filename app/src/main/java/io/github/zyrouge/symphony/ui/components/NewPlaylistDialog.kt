package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.entities.Playlist
import io.github.zyrouge.symphony.services.groove.entities.Song
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun NewPlaylistDialog(
    context: ViewContext,
    initialSongs: List<Song> = listOf(),
    onDone: (Playlist) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var input by remember { mutableStateOf("") }
    var showSongsPicker by remember { mutableStateOf(false) }
    val songs = remember { mutableStateListOf(*initialSongs.toTypedArray()) }
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
                Text(context.symphony.t.AddSongs + " (${songs.size})")
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                enabled = input.isNotBlank(),
                onClick = {
                    context.symphony.groove.coroutineScope.launch {
                        val playlist = context.symphony.groove.playlist.create { id ->
                            Playlist(
                                id = id,
                                title = input,
                                uri = null,
                                path = null,
                            )
                        }
                        if (songs.isNotEmpty()) {
                            context.symphony.groove.playlist.addSongs(playlist.id, songs)
                        }
                        withContext(Dispatchers.Main) {
                            onDone(playlist)
                        }
                    }
                }
            ) {
                Text(context.symphony.t.Done)
            }
        },
    )

    if (showSongsPicker) {
//        TODO
//        PlaylistManageSongsDialog(
//            context,
//            selectedSongIds = songIdsImmutable,
//            onDone = {
//                showSongsPicker = false
//                songIds.clear()
//                songIds.addAll(it)
//            }
//        )
    }
}