package io.github.zyrouge.symphony.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.view.helpers.Routes
import io.github.zyrouge.symphony.ui.view.helpers.ViewContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingBottomBar(context: ViewContext) {
    var currentPlayingSong by remember { mutableStateOf(Symphony.player.currentPlayingSong) }
    var isPlaying by remember { mutableStateOf(Symphony.player.isPlaying) }

    val unsubscribe = remember {
        Symphony.player.events.subscribe {
            currentPlayingSong = Symphony.player.currentPlayingSong
            isPlaying = Symphony.player.isPlaying
        }
    }

    DisposableEffect(LocalLifecycleOwner.current) {
        onDispose { unsubscribe() }
    }

    AnimatedVisibility(
        visible = currentPlayingSong != null,
        enter = slideIn {
            IntOffset(0, it.height / 2)
        } + fadeIn(),
        exit = slideOut {
            IntOffset(0, it.height / 2)
        } + fadeOut()
    ) {
        val song = currentPlayingSong!!
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RectangleShape,
            onClick = {
                context.navController.navigate(Routes.NowPlaying.route)
            }
        ) {
            Box(
                modifier = Modifier
                    .padding(12.dp, 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        modifier = Modifier
                            .size(45.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        bitmap = song.getArtwork()
                            .asImageBitmap(),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(song.title, style = MaterialTheme.typography.bodyMedium)
                        song.artistName?.let { artistName ->
                            Text(
                                artistName,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(15.dp))
                    IconButton(
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
                        enabled = Symphony.player.canJumpToNext(),
                        onClick = {
                            Symphony.player.jumpToNext()
                        }
                    ) {
                        Icon(Icons.Default.SkipNext, null)
                    }
                }
            }
        }
    }
}
