package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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

    Dialog(
        onDismissRequest = {
            onDone(allSelectedSongs.toList())
        }
    ) {
        Surface(modifier = Modifier.clip(RoundedCornerShape(8.dp))) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp, 0.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
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
                    Text(
                        context.symphony.t.manageSongs,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(20.dp, 0.dp),
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
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
                }
                Spacer(modifier = Modifier.height(12.dp))
                Divider()

                when {
                    allSongs.isEmpty() -> Text(
                        context.symphony.t.damnThisIsSoEmpty,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 20.dp),
                    )
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
        }
    }
}
