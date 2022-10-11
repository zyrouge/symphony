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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.components.NothingPlaying
import io.github.zyrouge.symphony.ui.components.PlayerAwareComp
import io.github.zyrouge.symphony.ui.view.helpers.Routes
import io.github.zyrouge.symphony.ui.view.helpers.ViewContext
import io.github.zyrouge.symphony.utils.DurationFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun NowPlayingView(context: ViewContext) {
    var song by remember { mutableStateOf(Symphony.player.currentPlayingSong) }
    var isPlaying by remember { mutableStateOf(Symphony.player.isPlaying) }
    var currentSongIndex by remember { mutableStateOf(Symphony.player.currentSongIndex) }
    var queueSize by remember { mutableStateOf(Symphony.player.queue.size) }

    BackHandler {
        context.navController.popBackStack()
    }

    PlayerAwareComp(
        context,
        onEvent = {
            song = Symphony.player.currentPlayingSong
            isPlaying = Symphony.player.isPlaying
            currentSongIndex = Symphony.player.currentSongIndex
            queueSize = Symphony.player.queue.size
        }
    ) {
        if (song == null) {
            NothingPlaying(context)
            return@PlayerAwareComp
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {},
                    colors = TopAppBarDefaults.mediumTopAppBarColors(
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
                        Image(
                            song!!.getArtwork().asImageBitmap(),
                            null,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
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
                                        MaterialTheme.colorScheme.contentColorFor(MaterialTheme.colorScheme.surface),
                                        CircleShape
                                    ),
                                    onClick = {
                                        if (Symphony.player.isPlaying) {
                                            Symphony.player.pause()
                                        } else {
                                            Symphony.player.resume()
                                        }
                                    }
                                ) {
                                    Icon(
                                        if (!isPlaying) Icons.Default.PlayArrow
                                        else Icons.Default.Pause,
                                        null,
                                        tint = MaterialTheme.colorScheme.surface
                                    )
                                }
                                IconButton(
                                    enabled = Symphony.player.canJumpToPrevious(),
                                    modifier = Modifier.background(
                                        MaterialTheme.colorScheme.surface,
                                        CircleShape
                                    ),
                                    onClick = {
                                        Symphony.player.jumpToPrevious()
                                    }
                                ) {
                                    Icon(Icons.Default.SkipPrevious, null)
                                }
                                IconButton(
                                    enabled = Symphony.player.canJumpToNext(),
                                    modifier = Modifier.background(
                                        MaterialTheme.colorScheme.surface,
                                        CircleShape
                                    ),
                                    onClick = {
                                        Symphony.player.jumpToNext()
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
                                var position by remember { mutableStateOf(Symphony.player.duration!!) }
                                val interactionSource = remember { MutableInteractionSource() }

                                var positionUpdater: Timer? = null
                                LaunchedEffect(LocalLifecycleOwner.current) {
                                    positionUpdater = kotlin.concurrent.timer(period = 100L) {
                                        position = Symphony.player.duration!!
                                    }
                                }
                                DisposableEffect(LocalLifecycleOwner.current) {
                                    onDispose {
                                        positionUpdater?.cancel()
                                    }
                                }

                                Text(
                                    DurationFormatter.formatAsMS(position.played),
                                    style = MaterialTheme.typography.labelMedium
                                )
                                BoxWithConstraints(modifier = Modifier.weight(1f)) {
                                    Slider(
                                        value = position.played.toFloat() / position.total.toFloat(),
                                        onValueChange = {},
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
                                    DurationFormatter.formatAsMS(position.total),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            },
            bottomBar = {
                Card(
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
                            Text(Symphony.t.playedXofY(currentSongIndex + 1, queueSize))
                            Row {
                                IconButton(
                                    onClick = {}
                                ) {
                                    Icon(Icons.Default.Repeat, null)
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
}
