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
                context.symphony.t.TrackName,
                song.title
            )
            InformationKeyValue(
                context.symphony.t.Artist,
                song.artistName ?: context.symphony.t.UnknownSymbol
            )
            InformationKeyValue(
                context.symphony.t.AlbumArtist,
                song.additional.albumArtist ?: context.symphony.t.UnknownSymbol
            )
            InformationKeyValue(
                context.symphony.t.Composer,
                song.composer ?: context.symphony.t.UnknownSymbol
            )
            song.year?.let {
                InformationKeyValue(context.symphony.t.Year, it.toString())
            }
            song.trackNumber?.let {
                InformationKeyValue(context.symphony.t.TrackNumber, it.toString())
            }
            InformationKeyValue(
                context.symphony.t.Duration,
                DurationFormatter.formatMs(song.duration)
            )
            song.additional.genre?.let {
                InformationKeyValue(context.symphony.t.Genre, it)
            }
            song.additional.bitrateK?.let {
                InformationKeyValue(
                    context.symphony.t.Bitrate,
                    context.symphony.t.XKbps(it.toString())
                )
            }
            song.additional.bitsPerSample?.let {
                InformationKeyValue(
                    context.symphony.t.BitDepth,
                    context.symphony.t.XBit(it.toString())
                )
            }
            song.additional.samplingRateK?.let {
                InformationKeyValue(
                    context.symphony.t.SamplingRate,
                    context.symphony.t.XKHz(it.toString())
                )
            }
            InformationKeyValue(
                context.symphony.t.Filename,
                song.filename
            )
            InformationKeyValue(
                context.symphony.t.Path,
                song.path
            )
            InformationKeyValue(
                context.symphony.t.Size,
                "${round((song.size / 1024 / 1024).toDouble())} MB"
            )
            InformationKeyValue(
                context.symphony.t.DateAdded,
                SimpleDateFormat.getInstance()
                    .format(Date(song.dateAdded))
            )
            InformationKeyValue(
                context.symphony.t.LastModified,
                SimpleDateFormat.getInstance()
                    .format(Date(song.dateModified))
            )
        },
        onDismissRequest = onDismissRequest,
    )
}
