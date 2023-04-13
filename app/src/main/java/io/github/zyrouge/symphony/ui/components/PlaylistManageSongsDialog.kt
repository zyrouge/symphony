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
import io.github.zyrouge.symphony.services.groove.SongRepository
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistManageSongsDialog(
    context: ViewContext,
    selectedSongs: List<Long>,
    onDone: (List<Long>) -> Unit,
) {
    val allSongs = remember {
        context.symphony.groove.song.getAll()
    }
    val nSelectedSongs = remember { selectedSongs.toMutableStateList() }
    var terms by remember { mutableStateOf("") }
    val songs by remember {
        derivedStateOf {
            SongRepository.search(allSongs, terms, null)
                .map { it.entity }
                .sortedBy { !selectedSongs.contains(it.id) }
        }
    }

    ScaffoldDialog(
        onDismissRequest = {
            onDone(nSelectedSongs.toList())
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
                        onDone(selectedSongs)
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
                        onDone(nSelectedSongs.toList())
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
                    songs.isEmpty() -> Box(modifier = Modifier.padding(0.dp, 12.dp)) {
                        SubtleCaptionText(context.symphony.t.DamnThisIsSoEmpty)
                    }
                    else -> BoxWithConstraints {
                        LazyColumn(
                            modifier = Modifier
                                .height(maxHeight)
                                .padding(bottom = 4.dp)
                        ) {
                            items(songs) { song ->
                                SongCard(
                                    context,
                                    song = song,
                                    thumbnailLabel = when {
                                        nSelectedSongs.contains(song.id) -> ({
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
                                        nSelectedSongs.contains(song.id) -> nSelectedSongs.remove(
                                            song.id
                                        )
                                        else -> nSelectedSongs.add(song.id)
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
