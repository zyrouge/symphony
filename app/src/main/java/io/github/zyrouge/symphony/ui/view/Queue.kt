package io.github.zyrouge.symphony.ui.view

import android.content.ClipData
import android.content.ClipDescription
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.Groove
import io.github.zyrouge.symphony.ui.components.IconButtonPlaceholderSize
import io.github.zyrouge.symphony.ui.components.NewPlaylistDialog
import io.github.zyrouge.symphony.ui.components.SongCard
import io.github.zyrouge.symphony.ui.components.TopAppBarMinimalTitle
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.nowPlaying.NothingPlayingBody
import io.github.zyrouge.symphony.utils.Logger
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object QueueViewRoute


const val QueueDragAndDropLabel = "symphony_song_drag_drop"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun QueueView(context: ViewContext) {
    val coroutineScope = rememberCoroutineScope()
    val queue by context.symphony.radio.observatory.queue.collectAsState()
    val queueIndex by context.symphony.radio.observatory.queueIndex.collectAsState()
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = queueIndex,
    )
    var showSaveDialog by remember { mutableStateOf(false) }

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
                    IconButton(
                        onClick = { showSaveDialog = !showSaveDialog }
                    ) {
                        Icon(Icons.Default.Save, null)
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
                            contentType = { _, _ -> Groove.Kind.SONG },
                        ) { i, songId ->
                            context.symphony.groove.song.get(songId)?.let { song ->
                                var dragBackground by remember { mutableStateOf(Color.Transparent) }
                                Box(
                                    Modifier
                                        .background(dragBackground)
                                        .dragAndDropTarget(
                                            shouldStartDragAndDrop = {
                                                //filter out most foreign drag&drops, can be simplified
                                                    event ->
                                                event.run {
                                                    if (!event.mimeTypes()
                                                            .contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                                                    ) {
                                                        Logger.warn("DropTarget", "Wrong Mimetype")
                                                        return@run false
                                                    }
                                                    if (event.toAndroidDragEvent().clipDescription.label != QueueDragAndDropLabel) {
                                                        Logger.warn(
                                                            "DropTarget",
                                                            "Not $QueueDragAndDropLabel Label"
                                                        )
                                                        return@run false
                                                    }
                                                    if (event.toAndroidDragEvent().localState == null) {
                                                        Logger.warn("DropTarget", "localState null")
                                                        return@run false
                                                    }
                                                    if (event.toAndroidDragEvent().localState !is List<*>) {
                                                        Logger.warn(
                                                            "DropTarget",
                                                            "Wrong ClipData localState Type ${event.toAndroidDragEvent().localState}"
                                                        )
                                                        return@run false
                                                    }
                                                    if ((event.toAndroidDragEvent().localState as List<*>).size != 2) {
                                                        Logger.warn(
                                                            "DropTarget",
                                                            "Wrong List size ${(event.toAndroidDragEvent().localState as List<*>).size}"
                                                        )
                                                        return@run false
                                                    }
                                                    return@run true
                                                }
                                            },
                                            target = remember {
                                                object : DragAndDropTarget {
                                                    override fun onEntered(event: DragAndDropEvent) {
                                                        dragBackground =
                                                            Color.DarkGray.copy(alpha = 0.35f)
                                                    }

                                                    override fun onExited(event: DragAndDropEvent) {
                                                        dragBackground = Color.Transparent
                                                    }

                                                    override fun onDrop(event: DragAndDropEvent): Boolean {
                                                        val list =
                                                            event.toAndroidDragEvent().localState as List<*>
                                                        val droppedI: Int =
                                                            list[0].toString().toInt()
                                                        val droppedSongId: String =
                                                            list[1].toString()
                                                        if (droppedI == i) {
                                                            return true
                                                        }
                                                        //always put after the dropped song
                                                        val newIndex =
                                                            if (droppedI < i) i else i + 1
                                                        //TODO: make something else when queueIndex == droppedI
                                                        // otherwise we swap the current playing song which isn't intended
                                                        context.symphony.radio.queue.remove(droppedI)
                                                        context.symphony.radio.queue.add(
                                                            droppedSongId,
                                                            index = newIndex
                                                        )
                                                        return true
                                                    }
                                                }
                                            }
                                        )
                                ) {
                                    Box {
                                        SongCard(
                                            context,
                                            song,
                                            autoHighlight = false,
                                            highlighted = i == queueIndex,
                                            leading = {
                                                Icon(Icons.Filled.DragIndicator, null)
                                                Spacer(modifier = Modifier.width(8.dp))
                                            },
                                            thumbnailLabel = {
                                                Text((i + 1).toString())
                                            },
                                            cardModifier = Modifier.dragAndDropSource {
                                                detectTapGestures(
                                                    onLongPress = {
                                                        startTransfer(
                                                            DragAndDropTransferData(
                                                                ClipData.newPlainText(
                                                                    QueueDragAndDropLabel,
                                                                    ""
                                                                ),
                                                                localState = listOf(
                                                                    i.toString(),
                                                                    songId
                                                                )
                                                            )
                                                        )
                                                    }
                                                )
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
                                                        MaterialTheme.colorScheme.background.copy(
                                                            alpha = 0.3f
                                                        )
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
        }
    )

    if (showSaveDialog) {
        NewPlaylistDialog(
            context,
            initialSongIds = queue.toList(),
            onDone = { playlist ->
                showSaveDialog = false
                context.symphony.groove.playlist.add(playlist)
            },
            onDismissRequest = {
                showSaveDialog = false
            }
        )
    }
}
