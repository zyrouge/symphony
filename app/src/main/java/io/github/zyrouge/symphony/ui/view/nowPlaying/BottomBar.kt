package io.github.zyrouge.symphony.ui.view.nowPlaying

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.launch
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MotionPhotosPaused
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.radio.RadioLoopMode
import io.github.zyrouge.symphony.ui.helpers.Routes
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.navigate
import io.github.zyrouge.symphony.ui.view.NowPlayingData
import io.github.zyrouge.symphony.ui.view.NowPlayingDefaults
import io.github.zyrouge.symphony.ui.view.NowPlayingLyricsLayout
import io.github.zyrouge.symphony.ui.view.NowPlayingStates
import io.github.zyrouge.symphony.utils.Logger
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingBodyBottomBar(
    context: ViewContext,
    data: NowPlayingData,
    states: NowPlayingStates,
) {
    val coroutineScope = rememberCoroutineScope()
    val equalizerActivity = rememberLauncherForActivityResult(
        context.symphony.radio.session.createEqualizerActivityContract()
    ) {}

    val sleepTimer by context.symphony.radio.observatory.sleepTimer.collectAsState()
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showPitchDialog by remember { mutableStateOf(false) }
    var showExtraOptions by remember { mutableStateOf(false) }

    data.run {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 8.dp,
                    end = 8.dp,
                    bottom = 4.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = {
                    context.navController.navigate(Routes.Queue)
                }
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Sort,
                    null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    context.symphony.t.PlayingXofY(
                        (currentSongIndex + 1).toString(),
                        queueSize.toString(),
                    ),
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            states.showLyrics.let { showLyricsState ->
                val showLyrics by showLyricsState.collectAsState()

                IconButton(
                    onClick = {
                        when (lyricsLayout) {
                            NowPlayingLyricsLayout.ReplaceArtwork -> {
                                val nShowLyrics = !showLyricsState.value
                                showLyricsState.value = nShowLyrics
                                NowPlayingDefaults.showLyrics = nShowLyrics
                            }

                            NowPlayingLyricsLayout.SeparatePage -> {
                                context.navController.navigate(Routes.Lyrics)
                            }
                        }
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.Article,
                        null,
                        tint = when {
                            showLyrics -> MaterialTheme.colorScheme.primary
                            else -> LocalContentColor.current
                        }
                    )
                }
            }
            IconButton(
                onClick = {
                    context.symphony.radio.queue.toggleLoopMode()
                }
            ) {
                Icon(
                    when (currentLoopMode) {
                        RadioLoopMode.Song -> Icons.Filled.RepeatOne
                        else -> Icons.Filled.Repeat
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
                    Icons.Filled.Shuffle,
                    null,
                    tint = if (!currentShuffleMode) LocalContentColor.current
                    else MaterialTheme.colorScheme.primary
                )
            }
            IconButton(
                onClick = {
                    showExtraOptions = !showExtraOptions
                }
            ) {
                Icon(Icons.Outlined.MoreHoriz, null)
            }
        }

        if (showSleepTimerDialog) {
            sleepTimer?.let {
                NowPlayingSleepTimerDialog(
                    context,
                    sleepTimer = it,
                    onDismissRequest = {
                        showSleepTimerDialog = false
                    }
                )
            } ?: run {
                NowPlayingSleepTimerSetDialog(
                    context,
                    onDismissRequest = {
                        showSleepTimerDialog = false
                    }
                )
            }
        }

        if (showSpeedDialog) {
            NowPlayingSpeedDialog(
                context,
                currentSpeed = data.currentSpeed,
                persistedSpeed = data.persistedSpeed,
                onDismissRequest = {
                    showSpeedDialog = false
                }
            )
        }

        if (showPitchDialog) {
            NowPlayingPitchDialog(
                context,
                currentPitch = data.currentPitch,
                persistedPitch = data.persistedPitch,
                onDismissRequest = {
                    showPitchDialog = false
                }
            )
        }

        if (showExtraOptions) {
            val sheetState = rememberModalBottomSheetState()
            val closeBottomSheet = {
                showExtraOptions = false
                coroutineScope.launch {
                    sheetState.hide()
                }
            }

            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = {
                    showExtraOptions = false
                },
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Card(
                        onClick = {
                            closeBottomSheet()
                            try {
                                equalizerActivity.launch()
                            } catch (err: Exception) {
                                Logger.error(
                                    "NowPlayingBottomBar",
                                    "launching equalizer failed",
                                    err
                                )
                                Toast.makeText(
                                    context.activity,
                                    context.symphony.t.LaunchingEqualizerFailedX(
                                        err.localizedMessage ?: err.toString()
                                    ),
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                    ) {
                        ListItem(
                            leadingContent = {
                                Icon(Icons.Filled.GraphicEq, null)
                            },
                            headlineContent = {
                                Text(context.symphony.t.Equalizer)
                            },
                        )
                    }
                    Card(
                        onClick = {
                            closeBottomSheet()
                            context.symphony.radio.setPauseOnCurrentSongEnd(!pauseOnCurrentSongEnd)
                        }
                    ) {
                        ListItem(
                            leadingContent = {
                                Icon(
                                    Icons.Filled.MotionPhotosPaused,
                                    null,
                                    tint = when {
                                        pauseOnCurrentSongEnd -> MaterialTheme.colorScheme.primary
                                        else -> LocalContentColor.current
                                    }
                                )
                            },
                            headlineContent = {
                                Text(context.symphony.t.PauseOnCurrentSongEnd)
                            },
                            supportingContent = {
                                Text(
                                    if (pauseOnCurrentSongEnd) context.symphony.t.Enabled
                                    else context.symphony.t.Disabled
                                )
                            },
                        )
                    }
                    Card(
                        onClick = {
                            closeBottomSheet()
                            showSleepTimerDialog = !showSleepTimerDialog
                        }
                    ) {
                        ListItem(
                            leadingContent = {
                                Icon(
                                    Icons.Outlined.Timer,
                                    null,
                                    tint = when {
                                        hasSleepTimer -> MaterialTheme.colorScheme.primary
                                        else -> LocalContentColor.current
                                    }
                                )
                            },
                            headlineContent = {
                                Text(context.symphony.t.SleepTimer)
                            },
                            supportingContent = {
                                Text(
                                    if (hasSleepTimer) context.symphony.t.Enabled
                                    else context.symphony.t.Disabled
                                )
                            },
                        )
                    }
                    Card(
                        onClick = {
                            closeBottomSheet()
                            showSpeedDialog = !showSpeedDialog
                        }
                    ) {
                        ListItem(
                            leadingContent = {
                                Icon(Icons.Outlined.Speed, null)
                            },
                            headlineContent = {
                                Text(context.symphony.t.Speed)
                            },
                            supportingContent = {
                                Text("x${data.currentSpeed}")
                            },
                        )
                    }
                    Card(
                        onClick = {
                            closeBottomSheet()
                            showPitchDialog = !showPitchDialog
                        }
                    ) {
                        ListItem(
                            leadingContent = {
                                Icon(Icons.Outlined.Speed, null)
                            },
                            headlineContent = {
                                Text(context.symphony.t.Pitch)
                            },
                            supportingContent = {
                                Text("x${data.currentPitch}")
                            },
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
