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
import io.github.zyrouge.symphony.services.groove.Playlist
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun NewPlaylistDialog(
    context: ViewContext,
    initialSongIds: List<Long> = listOf(),
    onDone: (Playlist) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var input by remember { mutableStateOf("") }
    var showSongsPicker by remember { mutableStateOf(false) }
    val songIds = remember { mutableStateListOf<Long>(*initialSongIds.toTypedArray()) }
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