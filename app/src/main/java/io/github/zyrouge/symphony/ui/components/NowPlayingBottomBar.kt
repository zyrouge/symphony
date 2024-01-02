package io.github.zyrouge.symphony.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.helpers.FadeTransition
import io.github.zyrouge.symphony.ui.helpers.Routes
import io.github.zyrouge.symphony.ui.helpers.TransitionDurations
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.navigate
import io.github.zyrouge.symphony.utils.runIfOrThis
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingBottomBar(context: ViewContext, drawInset: Boolean = true) {
    val queue by context.symphony.radio.observatory.queue.collectAsState()
    val queueIndex by context.symphony.radio.observatory.queueIndex.collectAsState()
    val currentPlayingSong by remember(queue, queueIndex) {
        derivedStateOf {
            queue.getOrNull(queueIndex)?.let { context.symphony.groove.song.get(it) }
        }
    }
    val isPlaying by context.symphony.radio.observatory.isPlaying.collectAsState()
    val playbackPosition by context.symphony.radio.observatory.playbackPosition.collectAsState()
    val showTrackControls by context.symphony.settings.miniPlayerTrackControls.collectAsState()
    val showSeekControls by context.symphony.settings.miniPlayerSeekControls.collectAsState()
    val seekBackDuration by context.symphony.settings.seekBackDuration.collectAsState()
    val seekForwardDuration by context.symphony.settings.seekForwardDuration.collectAsState()

    AnimatedContent(
        label = "c-now-playing-container",
        modifier = Modifier.fillMaxWidth(),
        targetState = currentPlayingSong,
        contentKey = { it != null },
        transitionSpec = {
            slideInVertically().plus(fadeIn())
                .togetherWith(slideOutVertically().plus(fadeOut()))
        }
    ) { currentPlayingSongTarget ->
        currentPlayingSongTarget?.let { currentSong ->
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
            ) {
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(0.3f))
                            .fillMaxWidth()
                            .fillMaxHeight()
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .background(MaterialTheme.colorScheme.primary)
                            .fillMaxWidth(playbackPosition.ratio)
                            .fillMaxHeight()
                    )
                }
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .swipeable(
                            onSwipeUp = {
                                context.navController.navigate(Routes.NowPlaying)
                            },
                            onSwipeDown = {
                                context.symphony.radio.stop()
                            },
                        ),
                    shape = RectangleShape,
                    onClick = {
                        context.navController.navigate(Routes.NowPlaying)
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(0.dp, 8.dp),
                    ) {
                        Spacer(modifier = Modifier.width(12.dp))
                        AnimatedContent(
                            label = "c-now-playing-card-image",
                            targetState = currentSong,
                            transitionSpec = {
                                val from = fadeIn(
                                    animationSpec = TransitionDurations.Normal.asTween(delayMillis = 150),
                                )
                                val to = fadeOut(animationSpec = TransitionDurations.Fast.asTween())
                                from togetherWith to
                            },
                        ) { song ->
                            AsyncImage(
                                song.createArtworkImageRequest(context.symphony).build(),
                                null,
                                modifier = Modifier
                                    .size(45.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        }
                        Spacer(modifier = Modifier.width(15.dp))
                        AnimatedContent(
                            label = "c-now-playing-card-content",
                            modifier = Modifier.weight(1f),
                            targetState = currentSong,
                            transitionSpec = {
                                val from = fadeIn(
                                    animationSpec = TransitionDurations.Normal.asTween(delayMillis = 150),
                                ) + scaleIn(
                                    initialScale = 0.99f,
                                    animationSpec = TransitionDurations.Normal.asTween(delayMillis = 150),
                                )
                                val to = fadeOut(animationSpec = TransitionDurations.Fast.asTween())
                                from togetherWith to
                            },
                        ) { song ->
                            NowPlayingBottomBarContent(context, song = song)
                        }
                        Spacer(modifier = Modifier.width(15.dp))
                        if (showTrackControls) {
                            IconButton(
                                onClick = { context.symphony.radio.shorty.previous() }
                            ) {
                                Icon(Icons.Filled.SkipPrevious, null)
                            }
                        }
                        if (showSeekControls) {
                            IconButton(
                                onClick = {
                                    context.symphony.radio.shorty.seekFromCurrent(-seekBackDuration)
                                }
                            ) {
                                Icon(Icons.Filled.FastRewind, null)
                            }
                        }
                        IconButton(
                            onClick = { context.symphony.radio.shorty.playPause() }
                        ) {
                            Icon(
                                when {
                                    !isPlaying -> Icons.Filled.PlayArrow
                                    else -> Icons.Filled.Pause
                                },
                                null
                            )
                        }
                        if (showSeekControls) {
                            IconButton(
                                onClick = {
                                    context.symphony.radio.shorty.seekFromCurrent(
                                        seekForwardDuration
                                    )
                                }
                            ) {
                                Icon(Icons.Filled.FastForward, null)
                            }
                        }
                        if (showTrackControls) {
                            IconButton(
                                onClick = { context.symphony.radio.shorty.skip() }
                            ) {
                                Icon(Icons.Filled.SkipNext, null)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
                if (drawInset) {
                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            }
        }
    }
}

@Composable
private fun NowPlayingBottomBarContent(context: ViewContext, song: Song) {
    BoxWithConstraints {
        val cardWidthPx = constraints.maxWidth
        var offsetX by remember { mutableFloatStateOf(0f) }
        val cardOffsetX = animateIntAsState(
            offsetX.toInt(),
            label = "c-now-playing-card-offset-x",
        )
        val cardOpacity = animateFloatAsState(
            if (offsetX != 0f) 0.7f else 1f,
            label = "c-now-playing-card-opacity",
        )

        Box(
            modifier = Modifier
                .alpha(cardOpacity.value)
                .absoluteOffset {
                    IntOffset(cardOffsetX.value.div(2), 0)
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val thresh = cardWidthPx / 4
                            offsetX = when {
                                -offsetX > thresh -> {
                                    val changed = context.symphony.radio.shorty.skip()
                                    if (changed) -cardWidthPx.toFloat() else 0f
                                }

                                offsetX > thresh -> {
                                    val changed = context.symphony.radio.shorty.previous()
                                    if (changed) cardWidthPx.toFloat() else 0f
                                }

                                else -> 0f
                            }
                        },
                        onDragCancel = {
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX += dragAmount
                        },
                    )
                },
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                NowPlayingBottomBarContentText(
                    context,
                    song.title,
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (song.artists.isNotEmpty()) {
                    NowPlayingBottomBarContentText(
                        context,
                        song.artists.joinToString(),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NowPlayingBottomBarContentText(
    context: ViewContext,
    text: String,
    style: TextStyle,
) {
    val textMarquee by context.symphony.settings.miniPlayerTextMarquee.collectAsState()
    var showOverlay by remember { mutableStateOf(false) }

    Box {
        Text(
            text,
            style = style,
            maxLines = 1,
            overflow = when {
                textMarquee -> TextOverflow.Clip
                else -> TextOverflow.Ellipsis
            },
            modifier = Modifier
                .runIfOrThis<Modifier>(textMarquee) {
                    basicMarquee(iterations = Int.MAX_VALUE)
                }
                .onGloballyPositioned {
                    val offsetX = it.boundsInParent().centerLeft.x
                    showOverlay = offsetX.absoluteValue != 0f
                },
        )
        AnimatedVisibility(
            visible = showOverlay,
            modifier = Modifier.matchParentSize(),
            enter = FadeTransition.enterTransition(),
            exit = FadeTransition.exitTransition(),
        ) {
            val backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)

            Row {
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(backgroundColor, Color.Transparent)
                            )
                        )
                ) {}
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, backgroundColor)
                            )
                        )
                ) {}
            }
        }
    }
}
