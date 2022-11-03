package io.github.zyrouge.symphony.ui.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.EventerEffect
import io.github.zyrouge.symphony.ui.components.SongCard
import io.github.zyrouge.symphony.ui.components.TopAppBarMinimalTitle
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueView(context: ViewContext) {
    var queue by remember { mutableStateOf(context.symphony.player.queue.toList()) }
    var currentSongIndex by remember { mutableStateOf(context.symphony.player.currentSongIndex) }
    val selectedSongIndices = remember { mutableStateListOf<Int>() }

    BackHandler {
        context.navController.popBackStack()
    }

    EventerEffect(context.symphony.player.onUpdate) {
        queue = context.symphony.player.queue.toList()
        currentSongIndex = context.symphony.player.currentSongIndex
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    TopAppBarMinimalTitle {
                        Text(context.symphony.t.queue)
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
                    if (selectedSongIndices.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                context.symphony.player.removeFromQueue(selectedSongIndices.toList())
                                selectedSongIndices.clear()
                            }
                        ) {
                            Icon(Icons.Default.Delete, null)
                        }
                    }
                    IconButton(
                        onClick = {
                            context.symphony.player.stop()
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
                    LazyColumn {
                        items(queue.size) { i ->
                            val song = queue[i]
                            Box(
                                modifier = Modifier
                                    .alpha(if (i < currentSongIndex) 0.7f else 1f)
                            ) {
                                SongCard(
                                    context,
                                    song,
                                    autoHighlight = false,
                                    highlighted = i == currentSongIndex,
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
                                        context.symphony.player.jumpTo(i)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}
