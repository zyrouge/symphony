package io.github.zyrouge.symphony.ui.view.nowPlaying

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.SongDropdownMenu
import io.github.zyrouge.symphony.ui.components.noRippleClickable
import io.github.zyrouge.symphony.ui.helpers.FadeTransition
import io.github.zyrouge.symphony.ui.helpers.RoutesBuilder
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.NowPlayingControlsLayout
import io.github.zyrouge.symphony.ui.view.NowPlayingPlayerStateData
import io.github.zyrouge.symphony.utils.DurationFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingBodyContent(context: ViewContext, data: NowPlayingPlayerStateData) {
    val playbackPosition by context.symphony.radio.observatory.playbackPosition.collectAsState()
    val favoriteSongIds = context.symphony.groove.playlist.favorites
    val isFavorite by remember {
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
                        targetStateSong.artistName?.let {
                            Text(
                                it,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.noRippleClickable {
                                    context.navController.navigate(
                                        RoutesBuilder.buildArtistRoute(it)
                                    )
                                },
                            )
                        }
                        if (data.showSongAdditionalInfo) {
                            targetStateSong.additional.toSamplingInfoString(context.symphony)?.let {
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
                NowPlayingControlsLayout.Default -> Row(
                    modifier = Modifier.padding(defaultHorizontalPadding, 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    NowPlayingPlayPauseButton(
                        context,
                        data = data,
                        style = NowPlayingControlButtonStyle(
                            color = NowPlayingControlButtonColors.Primary,
                        ),
                    )
                    NowPlayingSkipPreviousButton(
                        context,
                        data = data,
                        style = NowPlayingControlButtonStyle(
                            color = NowPlayingControlButtonColors.Surface,
                        ),
                    )
                    if (enableSeekControls) {
                        NowPlayingFastRewindButton(
                            context,
                            data = data,
                            style = NowPlayingControlButtonStyle(
                                color = NowPlayingControlButtonColors.Surface,
                            ),
                        )
                        NowPlayingFastForwardButton(
                            context,
                            data = data,
                            style = NowPlayingControlButtonStyle(
                                color = NowPlayingControlButtonColors.Surface,
                            ),
                        )
                    }
                    NowPlayingSkipNextButton(
                        context,
                        data = data,
                        style = NowPlayingControlButtonStyle(
                            color = NowPlayingControlButtonColors.Surface,
                        ),
                    )
                }

                NowPlayingControlsLayout.Traditional -> Row(
                    modifier = Modifier
                        .padding(defaultHorizontalPadding, 0.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    NowPlayingSkipPreviousButton(
                        context,
                        data = data,
                        style = NowPlayingControlButtonStyle(
                            color = NowPlayingControlButtonColors.Transparent,
                        ),
                    )
                    if (enableSeekControls) {
                        NowPlayingFastRewindButton(
                            context,
                            data = data,
                            style = NowPlayingControlButtonStyle(
                                color = NowPlayingControlButtonColors.Transparent,
                            ),
                        )
                    }
                    NowPlayingPlayPauseButton(
                        context,
                        data = data,
                        style = NowPlayingControlButtonStyle(
                            color = NowPlayingControlButtonColors.Surface,
                            size = NowPlayingControlButtonSize.Large,
                        ),
                    )
                    if (enableSeekControls) {
                        NowPlayingFastForwardButton(
                            context,
                            data = data,
                            style = NowPlayingControlButtonStyle(
                                color = NowPlayingControlButtonColors.Transparent,
                            ),
                        )
                    }
                    NowPlayingSkipNextButton(
                        context,
                        data = data,
                        style = NowPlayingControlButtonStyle(
                            color = NowPlayingControlButtonColors.Transparent,
                        ),
                    )
                }
            }
            Spacer(modifier = Modifier.height(defaultHorizontalPadding))
            Row(
                modifier = Modifier.padding(defaultHorizontalPadding, 0.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val interactionSource = remember { MutableInteractionSource() }
                var sliderPosition by remember { mutableStateOf<Int?>(null) }
                Text(
                    DurationFormatter.formatMs(sliderPosition ?: playbackPosition.played),
                    style = MaterialTheme.typography.labelMedium
                )
                Box(modifier = Modifier.weight(1f)) {
                    Slider(
                        value = (sliderPosition ?: playbackPosition.played).toFloat(),
                        valueRange = 0f..playbackPosition.total.toFloat(),
                        onValueChange = {
                            sliderPosition = it.toInt()
                        },
                        onValueChangeFinished = {
                            sliderPosition?.let {
                                context.symphony.radio.seek(it)
                                sliderPosition = null
                            }
                        },
                        interactionSource = interactionSource,
                        thumb = {
                            SliderDefaults.Thumb(
                                interactionSource = interactionSource,
                                thumbSize = DpSize(12.dp, 12.dp),
                                // NOTE: pad top to fix stupid layout
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    )
                }
                Text(
                    DurationFormatter.formatMs(playbackPosition.total),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun NowPlayingPlayPauseButton(
    context: ViewContext,
    data: NowPlayingPlayerStateData,
    style: NowPlayingControlButtonStyle
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
    data: NowPlayingPlayerStateData,
    style: NowPlayingControlButtonStyle
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
    data: NowPlayingPlayerStateData,
    style: NowPlayingControlButtonStyle
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
    data: NowPlayingPlayerStateData,
    style: NowPlayingControlButtonStyle
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
    data: NowPlayingPlayerStateData,
    style: NowPlayingControlButtonStyle
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

private enum class NowPlayingControlButtonColors {
    Primary,
    Surface,
    Transparent,
}

private enum class NowPlayingControlButtonSize {
    Default,
    Large,
}

private data class NowPlayingControlButtonStyle(
    val color: NowPlayingControlButtonColors,
    val size: NowPlayingControlButtonSize = NowPlayingControlButtonSize.Default,
)

@Composable
private fun NowPlayingControlButton(
    style: NowPlayingControlButtonStyle,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val backgroundColor = when (style.color) {
        NowPlayingControlButtonColors.Primary -> MaterialTheme.colorScheme.primary
        NowPlayingControlButtonColors.Surface -> MaterialTheme.colorScheme.surfaceVariant
        NowPlayingControlButtonColors.Transparent -> Color.Transparent
    }
    val contentColor = when (style.color) {
        NowPlayingControlButtonColors.Primary -> MaterialTheme.colorScheme.onPrimary
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
