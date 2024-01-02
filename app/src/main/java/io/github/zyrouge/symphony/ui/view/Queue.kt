package io.github.zyrouge.symphony.ui.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.GrooveKinds
import io.github.zyrouge.symphony.ui.components.IconButtonPlaceholderSize
import io.github.zyrouge.symphony.ui.components.NewPlaylistDialog
import io.github.zyrouge.symphony.ui.components.SongCard
import io.github.zyrouge.symphony.ui.components.TopAppBarMinimalTitle
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.nowPlaying.NothingPlayingBody
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueView(context: ViewContext) {
    val coroutineScope = rememberCoroutineScope()
    val queue by context.symphony.radio.observatory.queue.collectAsState()
    val queueIndex by context.symphony.radio.observatory.queueIndex.collectAsState()
    val selectedSongIndices = remember { mutableStateListOf<Int>() }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = queueIndex,
    )
    var showSaveDialog by remember { mutableStateOf(false) }

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
                            Icons.Filled.ExpandMore,
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
                            Icon(Icons.Filled.Delete, null)
                        }

                        else -> IconButton(
                            onClick = {
                                showSaveDialog = !showSaveDialog
                            }
                        ) {
                            Icon(Icons.Default.Save, null)
                        }
                    }

                    IconButton(
                        onClick = {
                            context.symphony.radio.stop()
                        }
                    ) {
                        Icon(Icons.Filled.ClearAll, null)
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

    if (showSaveDialog) {
        NewPlaylistDialog(
            context,
            initialSongIds = queue.toList(),
            onDone = { playlist ->
                showSaveDialog = false
                coroutineScope.launch {
                    context.symphony.groove.playlist.add(playlist)
                }
            },
            onDismissRequest = {
                showSaveDialog = false
            }
        )
    }
}
