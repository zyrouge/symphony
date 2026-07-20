package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.zyrouge.symphony.services.groove.entities.Song
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun PlaylistManageSongsDialog(
    context: ViewContext,
    selectedSongs: List<Song>,
    onDone: (added: List<Song>, removed: List<Song>) -> Unit,
) {
    val songsSortBy by context.symphony.settings.lastUsedSongsSortBy.flow.collectAsStateWithLifecycle()
    val songsSortReverse by context.symphony.settings.lastUsedSongsSortReverse.flow.collectAsStateWithLifecycle()
    val allSongs by context.symphony.groove.song.valuesAsFlow(songsSortBy, songsSortReverse)
        .collectAsStateWithLifecycle(emptyList())
    val allSongsMap by remember(allSongs) { derivedStateOf { allSongs.associateBy { it.id } } }
    val originalSongIds = remember { selectedSongs.map { it.id }.toSet() }
    val nSelectedSongIds = remember { selectedSongs.map { it.id }.toMutableStateList() }
    var terms by remember { mutableStateOf("") }
    val songIds by remember(allSongs, terms) {
        derivedStateOf {
            val songs = when {
                terms.isEmpty() -> allSongs
                else -> context.symphony.groove.song.search(terms)
            }
            songs.map { it.id }.sortedBy { !nSelectedSongIds.contains(it) }
        }
    }

    val done = {
        val added = nSelectedSongIds
            .filter { !originalSongIds.contains(it) }
            .mapNotNull { allSongsMap[it] }
        val removed = selectedSongs.filter { !nSelectedSongIds.contains(it.id) }
        onDone(added, removed)
    }

    ScaffoldDialog(
        onDismissRequest = { done() },
        title = {
            Text(context.symphony.t.ManageSongs)
        },
        titleLeading = {
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(CircleShape)
                    .clickable { done() },
            ) {
                Icon(
                    Icons.Filled.Close,
                    null,
                    modifier = Modifier.padding(8.dp),
                )
            }
        },
        titleTrailing = {
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clip(CircleShape)
                    .clickable { done() },
            ) {
                Icon(
                    Icons.Filled.Done,
                    null,
                    modifier = Modifier.padding(8.dp),
                )
            }
        },
        content = {
            Column {
                TextField(
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RectangleShape,
                    colors = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp).let {
                        TextFieldDefaults.colors(
                            focusedContainerColor = it,
                            unfocusedContainerColor = it,
                        )
                    },
                    placeholder = {
                        Text(context.symphony.t.SearchYourMusic)
                    },
                    value = terms,
                    onValueChange = { terms = it },
                )
                when {
                    allSongs.isEmpty() -> Box(modifier = Modifier.padding(0.dp, 12.dp)) {
                        SubtleCaptionText(context.symphony.t.DamnThisIsSoEmpty)
                    }

                    else -> BoxWithConstraints {
                        LazyColumn(
                            modifier = Modifier
                                .height(this@BoxWithConstraints.maxHeight)
                                .padding(bottom = 4.dp),
                        ) {
                            items(songIds) { songId ->
                                val song = allSongsMap[songId] ?: return@items
                                SongCard(
                                    context,
                                    song = song,
                                    thumbnailLabel = when {
                                        nSelectedSongIds.contains(song.id) -> ({
                                            Icon(
                                                Icons.Filled.Check,
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
        },
    )
}
