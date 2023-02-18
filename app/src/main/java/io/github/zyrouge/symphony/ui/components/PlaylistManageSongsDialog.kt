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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.swap

@Composable
fun PlaylistManageSongsDialog(
    context: ViewContext,
    selectedSongs: List<Long>,
    onDone: (List<Long>) -> Unit,
) {
    val allSongs = remember {
        context.symphony.groove.song.getAll().sortedBy {
            !selectedSongs.contains(it.id)
        }
    }
    val allSelectedSongs = remember {
        mutableStateListOf<Long>().apply { swap(selectedSongs) }
    }

    ScaffoldDialog(
        onDismissRequest = {
            onDone(allSelectedSongs.toList())
        },
        title = {
            Text(context.symphony.t.manageSongs)
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
                        onDone(allSelectedSongs.toList())
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
            when {
                allSongs.isEmpty() -> Box(modifier = Modifier.padding(0.dp, 12.dp)) {
                    SubtleCaptionText(context.symphony.t.damnThisIsSoEmpty)
                }
                else -> BoxWithConstraints {
                    LazyColumn(
                        modifier = Modifier
                            .height(maxHeight)
                            .padding(bottom = 4.dp)
                    ) {
                        items(allSongs) { song ->
                            SongCard(
                                context,
                                song = song,
                                thumbnailLabel = when {
                                    allSelectedSongs.contains(song.id) -> ({
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
                                    allSelectedSongs.contains(song.id) -> allSelectedSongs.remove(
                                        song.id
                                    )
                                    else -> allSelectedSongs.add(song.id)
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
