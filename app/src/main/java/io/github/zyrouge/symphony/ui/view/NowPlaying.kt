package io.github.zyrouge.symphony.ui.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.LoopMode
import io.github.zyrouge.symphony.services.PlayerDuration
import io.github.zyrouge.symphony.ui.components.EventerEffect
import io.github.zyrouge.symphony.ui.components.TopAppBarMinimalTitle
import io.github.zyrouge.symphony.ui.helpers.Routes
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.DurationFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingView(context: ViewContext) {
    var song by remember { mutableStateOf(context.symphony.player.currentPlayingSong) }
    var isPlaying by remember { mutableStateOf(context.symphony.player.isPlaying) }
    var currentSongIndex by remember { mutableStateOf(context.symphony.player.currentSongIndex) }
    var queueSize by remember { mutableStateOf(context.symphony.player.queue.size) }
    var currentLoopMode by remember { mutableStateOf(context.symphony.player.currentLoopMode) }

    BackHandler {
        context.navController.popBackStack()
    }

    EventerEffect(
        context.symphony.player.onUpdate,
        onEvent = {
            song = context.symphony.player.currentPlayingSong
            isPlaying = context.symphony.player.isPlaying
            currentSongIndex = context.symphony.player.currentSongIndex
            queueSize = context.symphony.player.queue.size
            currentLoopMode = context.symphony.player.currentLoopMode
        }
    )

    if (song == null) {
        NothingPlaying(context)
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            NowPlayingAppBar(context)
        },
        content = { contentPadding ->
            val defaultHorizontalPadding = 20.dp
            Box(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.padding(defaultHorizontalPadding, 0.dp)) {
                        Image(
                            song!!.getArtwork(context.symphony).asImageBitmap(),
                            null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(modifier = Modifier.padding(defaultHorizontalPadding)) {
                        Text(
                            song!!.title,
                            style = MaterialTheme.typography.headlineSmall
                                .copy(fontWeight = FontWeight.Bold)
                        )
                        if (song!!.artistName != null) {
                            Text(song!!.artistName!!)
                        }
                        Spacer(modifier = Modifier.height(defaultHorizontalPadding + 8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(
                                modifier = Modifier.background(
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                ),
                                onClick = {
                                    if (context.symphony.player.isPlaying) {
                                        context.symphony.player.pause()
                                    } else {
                                        context.symphony.player.resume()
                                    }
                                }
                            ) {
                                Icon(
                                    if (!isPlaying) Icons.Default.PlayArrow
                                    else Icons.Default.Pause,
                                    null,
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            IconButton(
                                enabled = context.symphony.player.canJumpToPrevious(),
                                modifier = Modifier.background(
                                    MaterialTheme.colorScheme.surface,
                                    CircleShape
                                ),
                                onClick = {
                                    context.symphony.player.jumpToPrevious()
                                }
                            ) {
                                Icon(Icons.Default.SkipPrevious, null)
                            }
                            IconButton(
                                enabled = context.symphony.player.canJumpToNext(),
                                modifier = Modifier.background(
                                    MaterialTheme.colorScheme.surface,
                                    CircleShape
                                ),
                                onClick = {
                                    context.symphony.player.jumpToNext()
                                }
                            ) {
                                Icon(Icons.Default.SkipNext, null)
                            }
                        }
                        Spacer(modifier = Modifier.height(defaultHorizontalPadding))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val interactionSource = remember { MutableInteractionSource() }
                            var sliderPosition by remember { mutableStateOf<Int?>(null) }
                            var duration by remember {
                                mutableStateOf(
                                    context.symphony.player.duration ?: PlayerDuration.zero
                                )
                            }

                            EventerEffect(
                                context.symphony.player.onDurationUpdate,
                                onEvent = {
                                    duration = it
                                }
                            )

                            Text(
                                DurationFormatter.formatAsMS(sliderPosition ?: duration.played),
                                style = MaterialTheme.typography.labelMedium
                            )
                            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                                Slider(
                                    value = sliderPosition?.toFloat() ?: duration.played.toFloat(),
                                    valueRange = 0f..duration.total.toFloat(),
                                    onValueChange = {
                                        sliderPosition = it.toInt()
                                    },
                                    onValueChangeFinished = {
                                        sliderPosition?.let {
                                            context.symphony.player.seek(it)
                                            sliderPosition = null
                                        }
                                    },
                                    interactionSource = interactionSource,
                                    thumb = {
                                        SliderDefaults.Thumb(
                                            interactionSource = interactionSource,
                                            thumbSize = DpSize(12.dp, 12.dp)
                                        )
                                    }
                                )
                            }
                            Text(
                                DurationFormatter.formatAsMS(duration.total),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
                onClick = {
                    context.navController.navigate(Routes.Queue.route)
                }
            ) {
                Column(modifier = Modifier.padding(16.dp, 4.dp, 4.dp, 4.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(context.symphony.t.playingXofY(currentSongIndex + 1, queueSize))
                        Row {
                            IconButton(
                                onClick = {
                                    context.symphony.player.toggleLoopMode()
                                }
                            ) {
                                Icon(
                                    when (currentLoopMode) {
                                        LoopMode.Song -> Icons.Default.RepeatOne
                                        else -> Icons.Default.Repeat
                                    },
                                    null,
                                    modifier = Modifier.alpha(
                                        if (currentLoopMode == LoopMode.None) 0.7f else 1f
                                    )
                                )
                            }
                            IconButton(
                                onClick = {}
                            ) {
                                Icon(Icons.Default.Shuffle, null)
                            }
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingAppBar(context: ViewContext) {
    CenterAlignedTopAppBar(
        title = {
            TopAppBarMinimalTitle {
                Text(context.symphony.t.nowPlaying)
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        ),
        navigationIcon = {
            IconButton(
                onClick = {
                    context.navController.popBackStack()
                }
            ) {
                Icon(
                    Icons.Default.ExpandMore,
                    null,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    )
}
