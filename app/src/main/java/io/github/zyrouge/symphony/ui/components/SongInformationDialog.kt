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
import io.github.zyrouge.symphony.utils.DurationFormatter
import io.github.zyrouge.symphony.utils.copyToClipboardWithToast
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.round

@Composable
fun SongInformationDialog(context: ViewContext, song: Song, onDismissRequest: () -> Unit) {
    InformationDialog(
        context,
        content = {
            InformationKeyValue(context.symphony.t.TrackName) {
                LongPressCopyableText(context, song.title)
            }
            if (song.artists.isNotEmpty()) {
                InformationKeyValue(context.symphony.t.Artist) {
                    LongPressCopyableAndTappableText(context, song.artists) {
                        onDismissRequest()
                        context.navController.navigate(Routes.Artist.build(it))
                    }
                }
            }
            if (song.additional.albumArtists.isNotEmpty()) {
                InformationKeyValue(context.symphony.t.AlbumArtist) {
                    LongPressCopyableAndTappableText(context, song.additional.albumArtists) {
                        onDismissRequest()
                        context.navController.navigate(Routes.AlbumArtist.build(it))
                    }
                }
            }
            if (song.composers.isNotEmpty()) {
                InformationKeyValue(context.symphony.t.Composer) {
                    // TODO composers page maybe?
                    LongPressCopyableAndTappableText(context, song.composers) {
                        onDismissRequest()
                        context.navController.navigate(Routes.Artist.build(it))
                    }
                }
            }
            context.symphony.groove.album.getIdFromSong(song)?.let { albumId ->
                InformationKeyValue(context.symphony.t.Album) {
                    LongPressCopyableAndTappableText(context, setOf(song.album!!)) {
                        onDismissRequest()
                        context.navController.navigate(Routes.Album.build(albumId))
                    }
                }
            }
            if (song.additional.genres.isNotEmpty()) {
                InformationKeyValue(context.symphony.t.Genre) {
                    LongPressCopyableAndTappableText(context, song.additional.genres) {
                        onDismissRequest()
                        context.navController.navigate(Routes.Genre.build(it))
                    }
                }
            }
            song.year?.let {
                InformationKeyValue(context.symphony.t.Year) {
                    LongPressCopyableText(context, it.toString())
                }
            }
            song.trackNumber?.let { raw ->
                val (discNumber, trackNumber) = when {
                    raw >= 1000 -> raw / 1000 to raw % 1000
                    else -> null to raw
                }
                discNumber?.let {
                    InformationKeyValue(context.symphony.t.DiscNumber) {
                        LongPressCopyableText(context, it.toString())
                    }
                }
                InformationKeyValue(context.symphony.t.TrackNumber) {
                    LongPressCopyableText(context, trackNumber.toString())
                }
            }
            InformationKeyValue(context.symphony.t.Duration) {
                LongPressCopyableText(context, DurationFormatter.formatMs(song.duration))
            }
            song.additional.codec?.let {
                InformationKeyValue(context.symphony.t.Codec) {
                    LongPressCopyableText(context, it)
                }
            }
            song.additional.bitrateK?.let {
                InformationKeyValue(context.symphony.t.Bitrate) {
                    LongPressCopyableText(context, context.symphony.t.XKbps(it.toString()))
                }
            }
            song.additional.bitsPerSample?.let {
                InformationKeyValue(context.symphony.t.BitDepth) {
                    LongPressCopyableText(context, context.symphony.t.XBit(it.toString()))
                }
            }
            song.additional.samplingRateK?.let {
                InformationKeyValue(context.symphony.t.SamplingRate) {
                    LongPressCopyableText(context, context.symphony.t.XKHz(it.toString()))
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
            InformationKeyValue(context.symphony.t.DateAdded) {
                LongPressCopyableText(
                    context,
                    SimpleDateFormat.getInstance().format(Date(song.dateAdded * 1000)),
                )
            }
            InformationKeyValue(context.symphony.t.LastModified) {
                LongPressCopyableText(
                    context,
                    SimpleDateFormat.getInstance()
                        .format(Date(song.dateModified * 1000)),
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
                            copyToClipboardWithToast(context, it)
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
