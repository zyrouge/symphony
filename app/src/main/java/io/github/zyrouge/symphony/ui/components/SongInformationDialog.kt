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
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

@Composable
fun SongInformationDialog(context: ViewContext, song: Song, onDismissRequest: () -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {
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
                    context.symphony.t.details,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                KeyValueTextComponent(
                    context.symphony.t.trackName,
                    song.title
                )
                KeyValueTextComponent(
                    context.symphony.t.artist,
                    song.artistName ?: context.symphony.t.unk
                )
                KeyValueTextComponent(
                    context.symphony.t.albumArtist,
                    song.albumArtist ?: context.symphony.t.unk
                )
                KeyValueTextComponent(
                    context.symphony.t.composer,
                    song.composer ?: context.symphony.t.unk
                )
                Text("")
                KeyValueTextComponent(
                    context.symphony.t.filename,
                    song.filename
                )
                KeyValueTextComponent(
                    context.symphony.t.path,
                    song.path
                )
                KeyValueTextComponent(
                    context.symphony.t.size,
                    "${round((song.size / 1024 / 1024).toDouble())} MB"
                )
                KeyValueTextComponent(
                    context.symphony.t.dateAdded,
                    SimpleDateFormat.getInstance()
                        .format(Date(song.dateAdded))
                )
                KeyValueTextComponent(
                    context.symphony.t.lastModified,
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