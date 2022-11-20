package io.github.zyrouge.symphony.ui.view

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import coil.compose.AsyncImage
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.services.radio.PlaybackPosition
import io.github.zyrouge.symphony.services.radio.RadioLoopMode
import io.github.zyrouge.symphony.ui.components.EventerEffect
import io.github.zyrouge.symphony.ui.components.SongDropdownMenu
import io.github.zyrouge.symphony.ui.components.TopAppBarMinimalTitle
import io.github.zyrouge.symphony.ui.helpers.*
import io.github.zyrouge.symphony.utils.DurationFormatter

private data class PlayerStateData(
    val song: Song,
    val isPlaying: Boolean,
    val currentSongIndex: Int,
    val queueSize: Int,
    val currentLoopMode: RadioLoopMode,
    val currentShuffleMode: Boolean,
)

@Composable
fun NowPlayingView(context: ViewContext) {
    var song by remember { mutableStateOf(context.symphony.radio.queue.currentPlayingSong) }
    var isPlaying by remember { mutableStateOf(context.symphony.radio.isPlaying) }
    var currentSongIndex by remember { mutableStateOf(context.symphony.radio.queue.currentSongIndex) }
    var queueSize by remember { mutableStateOf(context.symphony.radio.queue.originalQueue.size) }
    var currentLoopMode by remember { mutableStateOf(context.symphony.radio.queue.currentLoopMode) }
    var currentShuffleMode by remember { mutableStateOf(context.symphony.radio.queue.currentShuffleMode) }
    var isViable by remember { mutableStateOf(song != null) }

    BackHandler {
        context.navController.popBackStack()
    }

    EventerEffect(context.symphony.radio.onUpdate) {
        song = context.symphony.radio.queue.currentPlayingSong
        isPlaying = context.symphony.radio.isPlaying
        currentSongIndex = context.symphony.radio.queue.currentSongIndex
        queueSize = context.symphony.radio.queue.originalQueue.size
        currentLoopMode = context.symphony.radio.queue.currentLoopMode
        currentShuffleMode = context.symphony.radio.queue.currentShuffleMode
        isViable = song != null
    }

    if (isViable) {
        NowPlayingBody(
            context,
            PlayerStateData(
                song = song!!,
                isPlaying = isPlaying,
                currentSongIndex = currentSongIndex,
                queueSize = queueSize,
                currentLoopMode = currentLoopMode,
                currentShuffleMode = currentShuffleMode,
            )
        )
    } else NothingPlaying(context)
}

private val defaultHorizontalPadding = 20.dp

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

@Composable
fun NowPlayingLandscapeAppBar(context: ViewContext) {
    Row(
        modifier = Modifier.padding(defaultHorizontalPadding, 4.dp, 12.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TopAppBarMinimalTitle {
            Text(context.symphony.t.nowPlaying)
        }
        Box(modifier = Modifier.weight(1f))
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NowPlayingBody(context: ViewContext, data: PlayerStateData) {
    data.run {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val orientation = ScreenOrientation.fromConstraints(this@BoxWithConstraints)
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    if (orientation.isPortrait) {
                        NowPlayingAppBar(context)
                    }
                },
                content = { contentPadding ->
                    BoxWithConstraints(modifier = Modifier.padding(contentPadding)) {
                        when (orientation) {
                            ScreenOrientation.PORTRAIT -> Column(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.weight(1f))
                                NowPlayingBodyCover(context, data)
                                Box(modifier = Modifier.weight(1f))
                                Column {
                                    NowPlayingBodyContent(context, data)
                                    NowPlayingBodyBottomBar(context, data)
                                }
                            }
                            ScreenOrientation.LANDSCAPE -> Row(modifier = Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(0.dp, 12.dp)
                                ) {
                                    NowPlayingBodyCover(context, data)
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    Column {
                                        NowPlayingLandscapeAppBar(context)
                                        Box(modifier = Modifier.weight(1f))
                                        NowPlayingBodyContent(context, data)
                                        NowPlayingBodyBottomBar(context, data)
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun NowPlayingBodyCover(context: ViewContext, data: PlayerStateData) {
    data.run {
        BoxWithConstraints(modifier = Modifier.padding(defaultHorizontalPadding, 0.dp)) {
            val dimension = min(maxHeight, maxWidth)
            AsyncImage(
                createHandyAsyncImageRequest(
                    LocalContext.current,
                    song.getArtworkUri(context.symphony)
                ),
                null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(dimension)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NowPlayingBodyContent(context: ViewContext, data: PlayerStateData) {
    data.run {
        Column(modifier = Modifier.padding(0.dp, 12.dp)) {
            Row {
                Column(
                    modifier = Modifier
                        .padding(defaultHorizontalPadding, 0.dp)
                        .weight(1f)
                ) {
                    Text(
                        song.title,
                        style = MaterialTheme.typography.headlineSmall
                            .copy(fontWeight = FontWeight.Bold)
                    )
                    song.artistName?.let {
                        Text(it)
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
                                context.symphony.radio.shorty.playPause()
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
                            modifier = Modifier.background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape
                            ),
                            onClick = {
                                context.symphony.radio.shorty.previous()
                            }
                        ) {
                            Icon(
                                Icons.Default.SkipPrevious,
                                null
                            )
                        }
                        IconButton(
                            modifier = Modifier.background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape
                            ),
                            onClick = {
                                context.symphony.radio.shorty.skip()
                            }
                        ) {
                            Icon(
                                Icons.Default.SkipNext,
                                null,
                            )
                        }
                    }
                }
                Column {
                    var showOptionsMenu by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = {
                            showOptionsMenu = !showOptionsMenu
                        }
                    ) {
                        Icon(Icons.Default.MoreVert, null)
                        SongDropdownMenu(
                            context,
                            song,
                            expanded = showOptionsMenu,
                            onDismissRequest = {
                                showOptionsMenu = false
                            }
                        )
                    }
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
                var duration by remember {
                    mutableStateOf(
                        context.symphony.radio.currentPlaybackPosition
                            ?: PlaybackPosition.zero
                    )
                }
                EventerEffect(context.symphony.radio.onPlaybackPositionUpdate) {
                    duration = it
                }
                Text(
                    DurationFormatter.formatAsMS(sliderPosition ?: duration.played),
                    style = MaterialTheme.typography.labelMedium
                )
                BoxWithConstraints(modifier = Modifier.weight(1f)) {
                    Slider(
                        value = (sliderPosition ?: duration.played).toFloat(),
                        valueRange = 0f..duration.total.toFloat(),
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
                    DurationFormatter.formatAsMS(duration.total),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NowPlayingBodyBottomBar(context: ViewContext, data: PlayerStateData) {
    data.run {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
            onClick = {
                context.navController.navigate(Routes.Queue)
            }
        ) {
            Column(modifier = Modifier.padding(16.dp, 4.dp, 4.dp, 4.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        context.symphony.t.playingXofY(
                            currentSongIndex + 1,
                            queueSize
                        )
                    )
                    Row {
                        IconButton(
                            onClick = {
                                context.symphony.radio.queue.toggleLoopMode()
                            }
                        ) {
                            Icon(
                                when (currentLoopMode) {
                                    RadioLoopMode.Song -> Icons.Default.RepeatOne
                                    else -> Icons.Default.Repeat
                                },
                                null,
                                tint = when (currentLoopMode) {
                                    RadioLoopMode.None -> LocalContentColor.current
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                        }
                        IconButton(
                            onClick = {
                                context.symphony.radio.queue.toggleShuffleMode()
                            }
                        ) {
                            Icon(
                                Icons.Default.Shuffle,
                                null,
                                tint = if (!currentShuffleMode) LocalContentColor.current
                                else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
