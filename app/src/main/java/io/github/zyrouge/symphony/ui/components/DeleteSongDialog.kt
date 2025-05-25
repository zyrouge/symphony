package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun DeleteSongDialog(context: ViewContext, song: Song, onDismissRequest: () -> Unit) {
    InformationDialog(
        //TODO: change title from details to something normal
        context,
        content = {
            Text("Do you want to delete the Song\n ${song.title}?") //TODO: i18n
            Row(
                Modifier.fillMaxWidth(),
            ) {
                Button(
                    onClick = {
                        context.symphony.groove.delete(song)
                        onDismissRequest()
                    }
                ) {
                    Text("Yes") //TODO: i18n
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = onDismissRequest
                ) {
                    Text("No") //TODO: i18n
                }
            }
        },
        onDismissRequest = onDismissRequest,
    )
}
