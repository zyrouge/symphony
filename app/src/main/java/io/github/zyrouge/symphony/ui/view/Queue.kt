package io.github.zyrouge.symphony.ui.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.EventerEffect
import io.github.zyrouge.symphony.ui.components.SongCard
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueView(context: ViewContext) {
    var queue by remember { mutableStateOf(context.symphony.player.queue.toList()) }
    var currentSongIndex by remember { mutableStateOf(context.symphony.player.currentSongIndex) }

    BackHandler {
        context.navController.popBackStack()
    }

    EventerEffect(
        context.symphony.player.onUpdate,
        onEvent = {
            queue = context.symphony.player.queue.toList()
            currentSongIndex = context.symphony.player.currentSongIndex
        }
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(context.symphony.t.queue)
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
                }
            )
        },
        content = { contentPadding ->
            Box(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
            ) {
                LazyColumn {
                    items(queue.size) { i ->
                        val song = queue[i]
                        SongCard(
                            context,
                            song,
                            onClick = {
                                context.symphony.player.jumpTo(i)
                            }
                        )
                    }
                }
            }
        },
    )
}
