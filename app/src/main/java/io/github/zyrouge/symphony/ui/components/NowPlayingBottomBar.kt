package io.github.zyrouge.symphony.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.helpers.FadeTransition
import io.github.zyrouge.symphony.ui.helpers.Routes
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.navigate
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun NowPlayingBottomBar(context: ViewContext, drawInset: Boolean = true) {
    val queue = context.symphony.radio.observatory.queue
    val queueIndex by context.symphony.radio.observatory.queueIndex.collectAsState()
    val currentPlayingSong by remember {
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

    AnimatedVisibility(
        visible = currentPlayingSong != null,
        enter = slideIn {
            IntOffset(0, it.height / 2)
        } + fadeIn(),
        exit = slideOut {
            IntOffset(0, it.height / 2)
        } + fadeOut()
    ) {
        currentPlayingSong?.let { currentSong ->
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
                        .wrapContentHeight(),
                    shape = RectangleShape,
                    onClick = {
                        context.navController.navigate(Routes.NowPlaying)
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(0.dp, 8.dp),
                    ) {
                        AnimatedContent(
                            modifier = Modifier.weight(1f),
                            targetState = currentSong,
                            transitionSpec = {
                                fadeIn(
                                    animationSpec = tween(220, delayMillis = 90)
                                ) + scaleIn(
                                    initialScale = 0.99f,
                                    animationSpec = tween(220, delayMillis = 90)
                                ) with fadeOut(animationSpec = tween(90))
                            },
                        ) { song ->
                            BoxWithConstraints {
                                val cardWidthPx = constraints.maxWidth
                                var offsetX by remember { mutableFloatStateOf(0f) }
                                val cardOffsetX = animateIntAsState(offsetX.toInt())
                                val cardOpacity = animateFloatAsState(
                                    if (offsetX != 0f) 0.7f else 1f,
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
                                                            val changed =
                                                                context.symphony.radio.shorty.skip()
                                                            if (changed) -cardWidthPx.toFloat() else 0f
                                                        }

                                                        offsetX > thresh -> {
                                                            val changed =
                                                                context.symphony.radio.shorty.previous()
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
                                    NowPlayingBottomBarContent(context, song = song)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(15.dp))
                        if (showTrackControls) {
                            IconButton(
                                onClick = { context.symphony.radio.shorty.previous() }
                            ) {
                                Icon(Icons.Default.SkipPrevious, null)
                            }
                        }
                        if (showSeekControls) {
                            IconButton(
                                onClick = {
                                    context.symphony.radio.shorty.seekFromCurrent(-seekBackDuration)
                                }
                            ) {
                                Icon(Icons.Default.FastRewind, null)
                            }
                        }
                        IconButton(
                            onClick = { context.symphony.radio.shorty.playPause() }
                        ) {
                            Icon(
                                when {
                                    !isPlaying -> Icons.Default.PlayArrow
                                    else -> Icons.Default.Pause
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
                                Icon(Icons.Default.FastForward, null)
                            }
                        }
                        if (showTrackControls) {
                            IconButton(
                                onClick = { context.symphony.radio.shorty.skip() }
                            ) {
                                Icon(Icons.Default.SkipNext, null)
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
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(modifier = Modifier.width(12.dp))
        AsyncImage(
            song.createArtworkImageRequest(context.symphony).build(),
            null,
            modifier = Modifier
                .size(45.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Spacer(modifier = Modifier.width(15.dp))
        Column(modifier = Modifier.weight(1f)) {
            NowPlayingBottomBarContentText(
                song.title,
                style = MaterialTheme.typography.bodyMedium,
            )
            song.artistName?.let { artistName ->
                NowPlayingBottomBarContentText(
                    artistName,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NowPlayingBottomBarContentText(
    text: String,
    style: TextStyle,
) {
    var showOverlay by remember { mutableStateOf(false) }

    Box {
        Text(
            text,
            style = style,
            maxLines = 1,
            modifier = Modifier
                .basicMarquee(iterations = Int.MAX_VALUE)
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
