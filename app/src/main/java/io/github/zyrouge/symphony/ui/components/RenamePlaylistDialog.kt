package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.Playlist
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import kotlinx.coroutines.launch

@Composable
fun RenamePlaylistDialog(
    context: ViewContext,
    playlist: Playlist,
    onRename: () -> Unit = {},
    onDismissRequest: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var input by remember { mutableStateOf(playlist.title) }
    val focusRequester = remember { FocusRequester() }

    ScaffoldDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(context.symphony.t.RenamePlaylist)
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
            TextButton(onClick = onDismissRequest) {
                Text(context.symphony.t.Cancel)
            }
            TextButton(
                enabled = input.isNotBlank() && input != playlist.title,
                onClick = {
                    onRename()
                    onDismissRequest()
                    coroutineScope.launch {
                        context.symphony.groove.playlist.renamePlaylist(playlist, input)
                    }
                }
            ) {
                Text(context.symphony.t.Done)
            }
        },
    )
}
