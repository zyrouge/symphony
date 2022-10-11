package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.view.helpers.ViewContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

@Composable
fun SongInformationDialog(context: ViewContext, song: Song, onDismissRequest: () -> Unit) {
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp, 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    Symphony.t.details,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                KeyValueTextComponent(
                    Symphony.t.trackName,
                    song.title
                )
                KeyValueTextComponent(
                    Symphony.t.artist,
                    song.artistName ?: Symphony.t.unk
                )
                KeyValueTextComponent(
                    Symphony.t.albumArtist,
                    song.albumArtist ?: Symphony.t.unk
                )
                KeyValueTextComponent(
                    Symphony.t.composer,
                    song.composer ?: Symphony.t.unk
                )
                Text("")
                KeyValueTextComponent(
                    Symphony.t.filename,
                    song.filename
                )
                KeyValueTextComponent(
                    Symphony.t.path,
                    song.path
                )
                KeyValueTextComponent(
                    Symphony.t.size,
                    "${round((song.size / 1024 / 1024).toDouble())} MB"
                )
                KeyValueTextComponent(
                    Symphony.t.dateAdded,
                    SimpleDateFormat.getInstance()
                        .format(Date(song.dateAdded))
                )
                KeyValueTextComponent(
                    Symphony.t.lastModified,
                    SimpleDateFormat.getInstance()
                        .format(Date(song.dateModified))
                )
            }
        }
    }
}

@Composable
private fun KeyValueTextComponent(key: String, value: String) {
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("${key}: ")
            }
            append(value)
        }
    )
}