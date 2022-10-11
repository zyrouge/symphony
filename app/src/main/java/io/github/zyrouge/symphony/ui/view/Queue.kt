package io.github.zyrouge.symphony.ui.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.view.helpers.ViewContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun QueueView(context: ViewContext) {
    var song by remember { mutableStateOf(Symphony.player.currentPlayingSong!!) }
    var isPlaying by remember { mutableStateOf(Symphony.player.isPlaying) }

    val unsubscribe = remember {
        Symphony.player.events.subscribe {
            song = Symphony.player.currentPlayingSong!!
            isPlaying = Symphony.player.isPlaying
        }
    }

    DisposableEffect(LocalLifecycleOwner.current) {
        onDispose { unsubscribe() }
    }

    BackHandler {
        context.navController.popBackStack()
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
            Box(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Image(
                        song.getArtwork().asImageBitmap(),
                        null,
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth()
                    )
//                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.padding(12.dp)) {
                        IconButton(
                            modifier = Modifier.background(
                                MaterialTheme.colorScheme.surface,
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
                                null
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
                }
            }
        },
    )
}
