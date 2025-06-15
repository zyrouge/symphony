package io.github.zyrouge.symphony.ui.view.nowPlaying

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.SongDropdownMenu
import io.github.zyrouge.symphony.ui.helpers.FadeTransition
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.ArtistViewRoute
import io.github.zyrouge.symphony.ui.view.NowPlayingControlsLayout
import io.github.zyrouge.symphony.ui.view.NowPlayingData
import io.github.zyrouge.symphony.utils.DurationHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NowPlayingBodyContent(context: ViewContext, data: NowPlayingData) {
    val favoriteSongIds by context.symphony.groove.playlist.favorites.collectAsStateWithLifecycle()
    val isFavorite by remember(data) {
        derivedStateOf { favoriteSongIds.contains(data.song.id) }
    }

    data.run {
        Column {
            Row {
                AnimatedContent(
                    label = "now-playing-body-content",
                    modifier = Modifier.weight(1f),
                    targetState = song,
                    transitionSpec = {
                        FadeTransition.enterTransition()
                            .togetherWith(FadeTransition.exitTransition())
                    },
                ) { targetStateSong ->
                    Column(modifier = Modifier.padding(defaultHorizontalPadding, 0.dp)) {
                        Text(
                            targetStateSong.title,
                            style = MaterialTheme.typography.headlineSmall
                                .copy(fontWeight = FontWeight.Bold),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (targetStateSong.artists.isNotEmpty()) {
                            FlowRow {
                                targetStateSong.artists.forEachIndexed { i, it ->
                                    Text(
                                        it,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.pointerInput(Unit) {
                                            detectTapGestures { _ ->
                                                context.navController.navigate(ArtistViewRoute(it))
                                            }
                                        },
                                    )
                                    if (i != targetStateSong.artists.size - 1) {
                                        Text(", ")
                                    }
                                }
                            }
                        }
                        if (data.showSongAdditionalInfo) {
                            targetStateSong.toSamplingInfoString(context.symphony)?.let {
                                val localContentColor = LocalContentColor.current
                                Text(
                                    it,
                                    style = MaterialTheme.typography.labelSmall
                                        .copy(color = localContentColor.copy(alpha = 0.7f)),
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                    }
                }
                Row {
                    IconButton(
                        modifier = Modifier.offset(4.dp),
                        onClick = {
                            context.symphony.groove.playlist.run {
                                when {
                                    isFavorite -> unfavorite(song.id)
                                    else -> favorite(song.id)
                                }
                            }
                        }
                    ) {
                        when {
                            isFavorite -> Icon(
                                Icons.Filled.Favorite,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                            )

                            else -> Icon(Icons.Filled.FavoriteBorder, null)
                        }
                    }

                    var showOptionsMenu by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = {
                            showOptionsMenu = !showOptionsMenu
                        }
                    ) {
                        Icon(Icons.Filled.MoreVert, null)
                        SongDropdownMenu(
                            context,
                            song,
                            isFavorite = isFavorite,
                            expanded = showOptionsMenu,
                            onDismissRequest = {
                                showOptionsMenu = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(defaultHorizontalPadding + 8.dp))
            when (controlsLayout) {
                NowPlayingControlsLayout.CompactLeft -> NowPlayingCompactControls(
                    context,
                    data = data
                )

                NowPlayingControlsLayout.CompactRight -> NowPlayingCompactControls(
                    context,
                    data = data,
                    modifier = Modifier.align(Alignment.End)
                )

                NowPlayingControlsLayout.Traditional -> NowPlayingTraditionalControls(
                    context,
                    data = data,
                )
            }
            Spacer(modifier = Modifier.height(defaultHorizontalPadding + 8.dp))
            NowPlayingSeekBar(context)
            Spacer(modifier = Modifier.height(defaultHorizontalPadding))
        }
    }
}

@Composable
fun NowPlayingCompactControls(
    context: ViewContext,
    data: NowPlayingData,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(defaultHorizontalPadding, 0.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        NowPlayingPlayPauseButton(
            context,
            data = data,
            style = NowPlayingControlButtonStyle(
                color = NowPlayingControlButtonColor.Primary,
            ),
        )
        NowPlayingSkipPreviousButton(
            context,
            data = data,
            style = NowPlayingControlButtonStyle(
                color = NowPlayingControlButtonColor.Surface,
            ),
        )
        if (data.enableSeekControls) {
            NowPlayingFastRewindButton(
                context,
                data = data,
                style = NowPlayingControlButtonStyle(
                    color = NowPlayingControlButtonColor.Surface,
                ),
            )
            NowPlayingFastForwardButton(
                context,
                data = data,
                style = NowPlayingControlButtonStyle(
                    color = NowPlayingControlButtonColor.Surface,
                ),
            )
        }
        NowPlayingSkipNextButton(
            context,
            data = data,
            style = NowPlayingControlButtonStyle(
                color = NowPlayingControlButtonColor.Surface,
            ),
        )
    }
}

@Composable
fun NowPlayingTraditionalControls(context: ViewContext, data: NowPlayingData) {
    Row(
        modifier = Modifier
            .padding(defaultHorizontalPadding, 0.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        NowPlayingSkipPreviousButton(
            context,
            data = data,
            style = NowPlayingControlButtonStyle(
                color = NowPlayingControlButtonColor.Transparent,
            ),
        )
        if (data.enableSeekControls) {
            NowPlayingFastRewindButton(
                context,
                data = data,
                style = NowPlayingControlButtonStyle(
                    color = NowPlayingControlButtonColor.Transparent,
                ),
            )
        }
        NowPlayingPlayPauseButton(
            context,
            data = data,
            style = NowPlayingControlButtonStyle(
                color = NowPlayingControlButtonColor.Surface,
                size = NowPlayingControlButtonSize.Large,
            ),
        )
        if (data.enableSeekControls) {
            NowPlayingFastForwardButton(
                context,
                data = data,
                style = NowPlayingControlButtonStyle(
                    color = NowPlayingControlButtonColor.Transparent,
                ),
            )
        }
        NowPlayingSkipNextButton(
            context,
            data = data,
            style = NowPlayingControlButtonStyle(
                color = NowPlayingControlButtonColor.Transparent,
            ),
        )
    }
}

@Composable
fun NowPlayingSeekBar(context: ViewContext) {
    val playbackPosition by context.symphony.radio.observatory.playbackPosition.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier.padding(defaultHorizontalPadding, 0.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        var seekRatio by remember { mutableStateOf<Float?>(null) }

        NowPlayingPlaybackPositionText(
            seekRatio?.let { it * playbackPosition.total }?.toLong()
                ?: playbackPosition.played,
            Alignment.CenterStart,
        )
        Box(modifier = Modifier.weight(1f)) {
            NowPlayingSeekBar(
                ratio = playbackPosition.ratio,
                onSeekStart = {
                    seekRatio = 0f
                },
                onSeek = {
                    seekRatio = it
                },
                onSeekEnd = {
                    context.symphony.radio.seek((it * playbackPosition.total).toLong())
                    seekRatio = null
                },
                onSeekCancel = {
                    seekRatio = null
                },
            )
        }
        NowPlayingPlaybackPositionText(
            playbackPosition.total,
            Alignment.CenterEnd,
        )
    }
}

@Composable
private fun NowPlayingSeekBar(
    ratio: Float,
    onSeekStart: () -> Unit,
    onSeek: (Float) -> Unit,
    onSeekEnd: (Float) -> Unit,
    onSeekCancel: () -> Unit,
) {
    val sliderHeight = 12.dp
    val thumbSize = 12.dp
    val thumbSizeHalf = thumbSize.div(2)
    val trackHeight = 4.dp

    var dragging by remember { mutableStateOf(false) }
    var dragRatio by remember { mutableFloatStateOf(0f) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(sliderHeight),
        contentAlignment = Alignment.Center,
    ) {
        val sliderWidth = this@BoxWithConstraints.maxWidth

        Box(
            modifier = Modifier
                .height(sliderHeight)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            val tapRatio = (offset.x / sliderWidth.toPx()).coerceIn(0f..1f)
                            onSeekEnd(tapRatio)
                        }
                    )
                }
                .pointerInput(Unit) {
                    var offsetX = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            offsetX = offset.x
                            dragging = true
                            onSeekStart()
                        },
                        onDragEnd = {
                            onSeekEnd(dragRatio)
                            offsetX = 0f
                            dragging = false
                            dragRatio = 0f
                        },
                        onDragCancel = {
                            onSeekCancel()
                            offsetX = 0f
                            dragging = false
                            dragRatio = 0f
                        },
                        onHorizontalDrag = { pointer, dragAmount ->
                            pointer.consume()
                            offsetX += dragAmount
                            dragRatio = (offsetX / sliderWidth.toPx()).coerceIn(0f..1f)
                            onSeek(dragRatio)
                        },
                    )
                }
        )
        Box(
            modifier = Modifier
                .padding(thumbSizeHalf, 0.dp)
                .height(trackHeight)
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(thumbSizeHalf)
                )
        ) {
            Box(
                modifier = Modifier
                    .height(trackHeight)
                    .fillMaxWidth(if (dragging) dragRatio else ratio)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(thumbSizeHalf)
                    )
            )
        }
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(thumbSize)
                    .offset(
                        sliderWidth
                            .minus(thumbSizeHalf.times(2))
                            .times(if (dragging) dragRatio else ratio),
                        0.dp
                    )
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}

@Composable
private fun NowPlayingPlaybackPositionText(
    duration: Long,
    alignment: Alignment,
) {
    val textStyle = MaterialTheme.typography.labelMedium
    val durationFormatted = DurationHelper.formatMs(duration)

    Box(contentAlignment = alignment) {
        Text(
            "0".repeat(durationFormatted.length),
            style = textStyle.copy(color = Color.Transparent),
        )
        Text(
            durationFormatted,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun NowPlayingPlayPauseButton(
    context: ViewContext,
    data: NowPlayingData,
    style: NowPlayingControlButtonStyle,
) {
    data.run {
        NowPlayingControlButton(
            style = style,
            icon = when {
                !isPlaying -> Icons.Filled.PlayArrow
                else -> Icons.Filled.Pause
            },
            onClick = {
                context.symphony.radio.shorty.playPause()
            }
        )
    }
}

@Composable
private fun NowPlayingSkipPreviousButton(
    context: ViewContext,
    data: NowPlayingData,
    style: NowPlayingControlButtonStyle,
) {
    data.run {
        NowPlayingControlButton(
            style = style,
            icon = Icons.Filled.SkipPrevious,
            onClick = {
                context.symphony.radio.shorty.previous()
            }
        )
    }
}

@Composable
private fun NowPlayingSkipNextButton(
    context: ViewContext,
    data: NowPlayingData,
    style: NowPlayingControlButtonStyle,
) {
    data.run {
        NowPlayingControlButton(
            style = style,
            icon = Icons.Filled.SkipNext,
            onClick = {
                context.symphony.radio.shorty.skip()
            }
        )
    }
}

@Composable
private fun NowPlayingFastRewindButton(
    context: ViewContext,
    data: NowPlayingData,
    style: NowPlayingControlButtonStyle,
) {
    data.run {
        NowPlayingControlButton(
            style = style,
            icon = Icons.Filled.FastRewind,
            onClick = {
                context.symphony.radio.shorty
                    .seekFromCurrent(-seekBackDuration)
            }
        )
    }
}

@Composable
private fun NowPlayingFastForwardButton(
    context: ViewContext,
    data: NowPlayingData,
    style: NowPlayingControlButtonStyle,
) {
    data.run {
        NowPlayingControlButton(
            style = style,
            icon = Icons.Filled.FastForward,
            onClick = {
                context.symphony.radio.shorty
                    .seekFromCurrent(seekForwardDuration)
            }
        )
    }
}

private enum class NowPlayingControlButtonColor {
    Primary,
    Surface,
    Transparent,
}

private enum class NowPlayingControlButtonSize {
    Default,
    Large,
}

private data class NowPlayingControlButtonStyle(
    val color: NowPlayingControlButtonColor,
    val size: NowPlayingControlButtonSize = NowPlayingControlButtonSize.Default,
)

@Composable
private fun NowPlayingControlButton(
    style: NowPlayingControlButtonStyle,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val backgroundColor = when (style.color) {
        NowPlayingControlButtonColor.Primary -> MaterialTheme.colorScheme.primary
        NowPlayingControlButtonColor.Surface -> MaterialTheme.colorScheme.surfaceVariant
        NowPlayingControlButtonColor.Transparent -> Color.Transparent
    }
    val contentColor = when (style.color) {
        NowPlayingControlButtonColor.Primary -> MaterialTheme.colorScheme.onPrimary
        else -> LocalContentColor.current
    }
    val iconSize = when (style.size) {
        NowPlayingControlButtonSize.Default -> 24.dp
        NowPlayingControlButtonSize.Large -> 32.dp
    }

    IconButton(
        modifier = Modifier.background(backgroundColor, CircleShape),
        onClick = onClick,
    ) {
        Icon(
            icon,
            null,
            tint = contentColor,
            modifier = Modifier.size(iconSize),
        )
    }
}
