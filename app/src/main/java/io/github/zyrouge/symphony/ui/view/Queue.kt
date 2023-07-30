package io.github.zyrouge.symphony.ui.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.GrooveKinds
import io.github.zyrouge.symphony.ui.components.IconButtonPlaceholder
import io.github.zyrouge.symphony.ui.components.IconButtonPlaceholderSize
import io.github.zyrouge.symphony.ui.components.SongCard
import io.github.zyrouge.symphony.ui.components.TopAppBarMinimalTitle
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.nowPlaying.NothingPlayingBody
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueView(context: ViewContext) {
    val coroutineScope = rememberCoroutineScope()
    val queue = context.symphony.radio.observatory.queue
    val queueIndex by context.symphony.radio.observatory.queueIndex.collectAsState()
    val selectedSongIndices = remember { mutableStateListOf<Int>() }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = queueIndex,
    )

    BackHandler {
        context.navController.popBackStack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    TopAppBarMinimalTitle(
                        modifier = Modifier.padding(start = IconButtonPlaceholderSize)
                    ) {
                        Text(context.symphony.t.Queue)
                    }
                },
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
                },
                actions = {
                    when {
                        selectedSongIndices.isNotEmpty() -> IconButton(
                            onClick = {
                                context.symphony.radio.queue.remove(selectedSongIndices.toList())
                                selectedSongIndices.clear()
                            }
                        ) {
                            Icon(Icons.Default.Delete, null)
                        }

                        else -> IconButtonPlaceholder()
                    }

                    IconButton(
                        onClick = {
                            context.symphony.radio.stop()
                        }
                    ) {
                        Icon(Icons.Default.ClearAll, null)
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
                if (queue.isEmpty()) {
                    NothingPlayingBody(context)
                } else {
                    LazyColumn(state = listState) {
                        itemsIndexed(
                            queue,
                            key = { i, id -> "$i-$id" },
                            contentType = { _, _ -> GrooveKinds.SONG },
                        ) { i, songId ->
                            context.symphony.groove.song.get(songId)?.let { song ->
                                Box {
                                    SongCard(
                                        context,
                                        song,
                                        autoHighlight = false,
                                        highlighted = i == queueIndex,
                                        leading = {
                                            Checkbox(
                                                checked = selectedSongIndices.contains(i),
                                                onCheckedChange = {
                                                    if (selectedSongIndices.contains(i)) {
                                                        selectedSongIndices.remove(i)
                                                    } else {
                                                        selectedSongIndices.add(i)
                                                    }
                                                },
                                                modifier = Modifier.offset((-4).dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        },
                                        thumbnailLabel = {
                                            Text((i + 1).toString())
                                        },
                                        onClick = {
                                            context.symphony.radio.jumpTo(i)
                                            coroutineScope.launch {
                                                listState.animateScrollToItem(i)
                                            }
                                        },
                                    )
                                    if (i < queueIndex) {
                                        Box(
                                            modifier = Modifier
                                                .matchParentSize()
                                                .background(
                                                    MaterialTheme.colorScheme.background.copy(alpha = 0.3f)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
