package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistManageSongsDialog(
    context: ViewContext,
    selectedSongIds: List<Long>,
    onDone: (List<Long>) -> Unit,
) {
    val allSongIds = context.symphony.groove.song.all
    val nSelectedSongIds = remember { selectedSongIds.toMutableStateList() }
    var terms by remember { mutableStateOf("") }
    val songIds by remember {
        derivedStateOf {
            context.symphony.groove.song.search(allSongIds, terms, null)
                .map { it.entity }
                .sortedBy { !selectedSongIds.contains(it) }
        }
    }

    ScaffoldDialog(
        onDismissRequest = {
            onDone(nSelectedSongIds.toList())
        },
        title = {
            Text(context.symphony.t.ManageSongs)
        },
        titleLeading = {
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(CircleShape)
                    .clickable {
                        onDone(selectedSongIds)
                    },
            ) {
                Icon(
                    Icons.Default.Close,
                    null,
                    modifier = Modifier.padding(8.dp)
                )
            }
        },
        titleTrailing = {
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clip(CircleShape)
                    .clickable {
                        onDone(nSelectedSongIds.toList())
                    },
            ) {
                Icon(
                    Icons.Default.Done,
                    null,
                    modifier = Modifier.padding(8.dp)
                )
            }
        },
        content = {
            Column {
                TextField(
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RectangleShape,
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                    ),
                    placeholder = {
                        Text(context.symphony.t.SearchYourMusic)
                    },
                    value = terms,
                    onValueChange = {
                        terms = it
                    },
                )
                when {
                    songIds.isEmpty() -> Box(modifier = Modifier.padding(0.dp, 12.dp)) {
                        SubtleCaptionText(context.symphony.t.DamnThisIsSoEmpty)
                    }
                    else -> BoxWithConstraints {
                        LazyColumn(
                            modifier = Modifier
                                .height(maxHeight)
                                .padding(bottom = 4.dp)
                        ) {
                            items(songIds) { songId ->
                                context.symphony.groove.song.get(songId)?.let { song ->
                                    SongCard(
                                        context,
                                        song = song,
                                        thumbnailLabel = when {
                                            nSelectedSongIds.contains(song.id) -> ({
                                                Icon(
                                                    Icons.Default.Check,
                                                    null,
                                                    modifier = Modifier.size(12.dp),
                                                )
                                            })
                                            else -> null
                                        },
                                        disableHeartIcon = true,
                                    ) {
                                        when {
                                            nSelectedSongIds.contains(song.id) -> {
                                                nSelectedSongIds.remove(song.id)
                                            }
                                            else -> nSelectedSongIds.add(song.id)
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
}
