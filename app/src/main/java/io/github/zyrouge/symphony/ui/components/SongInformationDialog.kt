package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextDecoration
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.helpers.Routes
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.navigateTo
import io.github.zyrouge.symphony.utils.ActivityUtils
import io.github.zyrouge.symphony.utils.DurationUtils
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.round

@Composable
fun SongInformationDialog(context: ViewContext, song: Song, onDismissRequest: () -> Unit) {
    InformationDialog(
        context,
        content = {
            InformationKeyValue(context.symphony.t.Id) {
                LongPressCopyableText(context, song.id)
            }
            InformationKeyValue(context.symphony.t.TrackName) {
                LongPressCopyableText(context, song.title)
            }
            if (song.artists.isNotEmpty()) {
                InformationKeyValue(context.symphony.t.Artist) {
                    LongPressCopyableAndTappableText(context, song.artists) {
                        onDismissRequest()
                        context.navController.navigateTo(Routes.Artist.build(it))
                    }
                }
            }
            if (song.albumArtists.isNotEmpty()) {
                InformationKeyValue(context.symphony.t.AlbumArtist) {
                    LongPressCopyableAndTappableText(context, song.albumArtists) {
                        onDismissRequest()
                        context.navController.navigateTo(Routes.AlbumArtist.build(it))
                    }
                }
            }
            if (song.composers.isNotEmpty()) {
                InformationKeyValue(context.symphony.t.Composer) {
                    // TODO composers page maybe?
                    LongPressCopyableAndTappableText(context, song.composers) {
                        onDismissRequest()
                        context.navController.navigateTo(Routes.Artist.build(it))
                    }
                }
            }
            context.symphony.groove.album.getIdFromSong(song)?.let { albumId ->
                InformationKeyValue(context.symphony.t.Album) {
                    LongPressCopyableAndTappableText(context, setOf(song.album!!)) {
                        onDismissRequest()
                        context.navController.navigateTo(Routes.Album.build(albumId))
                    }
                }
            }
            if (song.genres.isNotEmpty()) {
                InformationKeyValue(context.symphony.t.Genre) {
                    LongPressCopyableAndTappableText(context, song.genres) {
                        onDismissRequest()
                        context.navController.navigateTo(Routes.Genre.build(it))
                    }
                }
            }
            song.date?.let {
                InformationKeyValue(context.symphony.t.Date) {
                    LongPressCopyableText(context, it.toString())
                }
            }
            song.year?.let {
                InformationKeyValue(context.symphony.t.Year) {
                    LongPressCopyableText(context, it.toString())
                }
            }
            song.trackNumber?.let {
                InformationKeyValue(context.symphony.t.TrackNumber) {
                    LongPressCopyableText(context, it.toString())
                }
            }
            song.trackTotal?.let {
                InformationKeyValue(context.symphony.t.TrackCount) {
                    LongPressCopyableText(context, it.toString())
                }
            }
            song.discNumber?.let {
                InformationKeyValue(context.symphony.t.DiscNumber) {
                    LongPressCopyableText(context, it.toString())
                }
            }
            song.discTotal?.let {
                InformationKeyValue(context.symphony.t.DiscTotal) {
                    LongPressCopyableText(context, it.toString())
                }
            }
            InformationKeyValue(context.symphony.t.Duration) {
                LongPressCopyableText(context, DurationUtils.formatMs(song.duration))
            }
            song.codec?.let {
                InformationKeyValue(context.symphony.t.Codec) {
                    LongPressCopyableText(context, it)
                }
            }
            song.encoder?.let {
                InformationKeyValue(context.symphony.t.Encoder) {
                    LongPressCopyableText(context, it)
                }
            }
            song.channels?.let {
                InformationKeyValue(context.symphony.t.AudioChannels) {
                    LongPressCopyableText(context, it.toString())
                }
            }
            song.bitrateK?.let {
                InformationKeyValue(context.symphony.t.Bitrate) {
                    val text = buildString {
                        append(context.symphony.t.XKbps(it.toString()))
                        if (song.variableBitrate) {
                            append(" (${context.symphony.t.Variable})")
                        }
                    }
                    LongPressCopyableText(context, text)
                }
            }
            song.minBitrateK?.let {
                InformationKeyValue(context.symphony.t.MinimumBitrate) {
                    LongPressCopyableText(context, context.symphony.t.XKbps(it.toString()))
                }
            }
            song.maxBitrateK?.let {
                InformationKeyValue(context.symphony.t.MaximumBitrate) {
                    LongPressCopyableText(context, context.symphony.t.XKbps(it.toString()))
                }
            }
            song.bitsPerSample?.let {
                InformationKeyValue(context.symphony.t.BitDepth) {
                    LongPressCopyableText(context, context.symphony.t.XBit(it.toString()))
                }
            }
            song.samplingRateK?.let {
                InformationKeyValue(context.symphony.t.SamplingRate) {
                    LongPressCopyableText(context, context.symphony.t.XKHz(it.toString()))
                }
            }
            song.samples?.let {
                InformationKeyValue(context.symphony.t.TotalSamples) {
                    LongPressCopyableText(context, it.toString())
                }
            }
            InformationKeyValue(context.symphony.t.Filename) {
                LongPressCopyableText(context, song.filename)
            }
            InformationKeyValue(context.symphony.t.Path) {
                LongPressCopyableText(context, song.path)
            }
            InformationKeyValue(context.symphony.t.Size) {
                LongPressCopyableText(context, "${round((song.size / 1024 / 1024).toDouble())} MB")
            }
            InformationKeyValue(context.symphony.t.LastModified) {
                LongPressCopyableText(
                    context,
                    SimpleDateFormat.getInstance().format(Date(song.dateModified * 1000)),
                )
            }
        },
        onDismissRequest = onDismissRequest,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LongPressCopyableAndTappableText(
    context: ViewContext,
    values: Set<String>,
    onTap: (String) -> Unit,
) {
    val textStyle = LocalTextStyle.current.copy(
        textDecoration = TextDecoration.Underline,
    )

    FlowRow {
        values.forEachIndexed { i, it ->
            Text(
                it,
                style = textStyle,
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { _ ->
                            ActivityUtils.copyToClipboardAndNotify(context.symphony, it)
                        },
                        onTap = { _ ->
                            onTap(it)
                        },
                    )
                },
            )
            if (i != values.size - 1) {
                Text(", ")
            }
        }
    }
}
