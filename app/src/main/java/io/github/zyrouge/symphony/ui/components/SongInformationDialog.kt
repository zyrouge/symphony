package io.github.zyrouge.symphony.ui.components

import androidx.compose.runtime.Composable
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.DurationFormatter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

@Composable
fun SongInformationDialog(context: ViewContext, song: Song, onDismissRequest: () -> Unit) {
    InformationDialog(
        context,
        content = {
            InformationKeyValue(
                context.symphony.t.trackName,
                song.title
            )
            InformationKeyValue(
                context.symphony.t.artist,
                song.artistName ?: context.symphony.t.unk
            )
            InformationKeyValue(
                context.symphony.t.albumArtist,
                song.additional.albumArtist ?: context.symphony.t.unk
            )
            InformationKeyValue(
                context.symphony.t.composer,
                song.composer ?: context.symphony.t.unk
            )
            song.year?.let {
                InformationKeyValue(context.symphony.t.year, it.toString())
            }
            song.trackNumber?.let {
                InformationKeyValue(context.symphony.t.trackNumber, it.toString())
            }
            InformationKeyValue(
                context.symphony.t.duration,
                DurationFormatter.formatMs(song.duration)
            )
            song.additional.genre?.let {
                InformationKeyValue(context.symphony.t.genre, it)
            }
            song.additional.bitrateK?.let {
                InformationKeyValue(
                    context.symphony.t.bitrate,
                    context.symphony.t.XKbps(it)
                )
            }
            song.additional.bitsPerSample?.let {
                InformationKeyValue(
                    context.symphony.t.bitDepth,
                    context.symphony.t.XBit(it)
                )
            }
            song.additional.samplingRateK?.let {
                InformationKeyValue(
                    context.symphony.t.samplingRate,
                    context.symphony.t.XKHz(it)
                )
            }
            InformationKeyValue(
                context.symphony.t.filename,
                song.filename
            )
            InformationKeyValue(
                context.symphony.t.path,
                song.path
            )
            InformationKeyValue(
                context.symphony.t.size,
                "${round((song.size / 1024 / 1024).toDouble())} MB"
            )
            InformationKeyValue(
                context.symphony.t.dateAdded,
                SimpleDateFormat.getInstance()
                    .format(Date(song.dateAdded))
            )
            InformationKeyValue(
                context.symphony.t.lastModified,
                SimpleDateFormat.getInstance()
                    .format(Date(song.dateModified))
            )
        },
        onDismissRequest = onDismissRequest,
    )
}
