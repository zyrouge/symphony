package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextDecoration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.zyrouge.symphony.services.groove.entities.Song
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.AlbumViewRoute
import io.github.zyrouge.symphony.ui.view.ArtistViewRoute
import io.github.zyrouge.symphony.ui.view.GenreViewRoute
import io.github.zyrouge.symphony.utils.ActivityHelper
import io.github.zyrouge.symphony.utils.DurationHelper
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.round

@Composable
fun SongInformationDialog(context: ViewContext, song: Song, onDismissRequest: () -> Unit) {
    val artists by context.symphony.groove.song.findArtistsOfIdAsFlow(song.id)
        .collectAsStateWithLifecycle(emptyList())
    val albumArtists by context.symphony.groove.song.findAlbumArtistsOfIdAsFlow(song.id)
        .collectAsStateWithLifecycle(emptyList())
    val composers by context.symphony.groove.song.findComposersOfIdAsFlow(song.id)
        .collectAsStateWithLifecycle(emptyList())
    val albums by context.symphony.groove.song.findAlbumsOfIdAsFlow(song.id)
        .collectAsStateWithLifecycle(emptyList())
    val genres by context.symphony.groove.song.findGenresOfIdAsFlow(song.id)
        .collectAsStateWithLifecycle(emptyList())

    InformationDialog(
        context,
        content = {
            InformationKeyValue(context.symphony.t.Id) {
                LongPressCopyableText(context, song.id)
            }
            InformationKeyValue(context.symphony.t.TrackName) {
                LongPressCopyableText(context, song.title)
            }
            if (artists.isNotEmpty()) {
                val artistNamesMap = artists.associate { it.entity.id to it.entity.name }
                InformationKeyValue(context.symphony.t.Artist) {
                    LongPressCopyableAndTappableText(context, artistNamesMap) {
                        onDismissRequest()
                        context.navController.navigate(ArtistViewRoute(it))
                    }
                }
            }
            if (albumArtists.isNotEmpty()) {
                val albumArtistNamesMap = albumArtists.associate { it.entity.id to it.entity.name }
                InformationKeyValue(context.symphony.t.AlbumArtist) {
                    LongPressCopyableAndTappableText(context, albumArtistNamesMap) {
                        onDismissRequest()
                        context.navController.navigate(ArtistViewRoute(it))
                    }
                }
            }
            if (composers.isNotEmpty()) {
                val composerNamesMap = albumArtists.associate { it.entity.id to it.entity.name }
                InformationKeyValue(context.symphony.t.Composer) {
                    // TODO composers page maybe?
                    LongPressCopyableAndTappableText(context, composerNamesMap) {
                        onDismissRequest()
//                        context.navController.navigate(ArtistViewRoute(it))
                    }
                }
            }
            if (albums.isNotEmpty()) {
                val albumNamesMap = albums.associate { it.entity.id to it.entity.name }
                InformationKeyValue(context.symphony.t.Composer) {
                    LongPressCopyableAndTappableText(context, albumNamesMap) {
                        onDismissRequest()
                        context.navController.navigate(AlbumViewRoute(it))
                    }
                }
            }
            if (genres.isNotEmpty()) {
                val genreNamesMap = albums.associate { it.entity.id to it.entity.name }
                InformationKeyValue(context.symphony.t.Genre) {
                    LongPressCopyableAndTappableText(context, genreNamesMap) {
                        onDismissRequest()
                        context.navController.navigate(GenreViewRoute(it))
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
                LongPressCopyableText(context, DurationHelper.formatMs(song.duration))
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
                    }
                    LongPressCopyableText(context, text)
                }
            }
            song.samplingRateK?.let {
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
private fun <T> LongPressCopyableAndTappableText(
    context: ViewContext,
    values: Map<T, String>,
    onTap: (T) -> Unit,
) {
    val textStyle = LocalTextStyle.current.copy(textDecoration = TextDecoration.Underline)

    FlowRow {
        values.entries.forEachIndexed { i, x ->
            Text(
                x.value,
                style = textStyle,
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { _ ->
                            ActivityHelper.copyToClipboardAndNotify(context.symphony, x.value)
                        },
                        onTap = { _ ->
                            onTap(x.key)
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
