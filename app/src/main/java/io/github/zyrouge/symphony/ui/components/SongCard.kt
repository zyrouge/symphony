package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.services.radio.RadioEvents
import io.github.zyrouge.symphony.ui.helpers.RoutesBuilder
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongCard(
    context: ViewContext,
    song: Song,
    highlighted: Boolean = false,
    autoHighlight: Boolean = true,
    disableHeartIcon: Boolean = false,
    leading: @Composable () -> Unit = {},
    thumbnailLabel: (@Composable () -> Unit)? = null,
    trailingOptionsContent: (@Composable ColumnScope.(() -> Unit) -> Unit)? = null,
    onClick: () -> Unit,
) {
    var isCurrentPlaying by remember {
        mutableStateOf(autoHighlight && song.id == context.symphony.radio.queue.currentPlayingSong?.id)
    }
    val favoriteSongIds by context.symphony.groove.playlist.favorites.collectAsState()
    val isFavorite by remember {
        derivedStateOf { favoriteSongIds.contains(song.id) }
    }

    if (autoHighlight) {
        EventerEffect(context.symphony.radio.onUpdate) {
            if (it == RadioEvents.StartPlaying || it == RadioEvents.StopPlaying) {
                isCurrentPlaying = song.id == context.symphony.radio.queue.currentPlayingSong?.id
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        onClick = onClick
    ) {
        Box(modifier = Modifier.padding(12.dp, 12.dp, 4.dp, 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                leading()
                Box {
                    AsyncImage(
                        song.createArtworkImageRequest(context.symphony).build(),
                        null,
                        modifier = Modifier
                            .size(45.dp)
                            .clip(RoundedCornerShape(10.dp)),
                    )
                    thumbnailLabel?.let { it ->
                        Box(
                            modifier = Modifier
                                .offset(y = 8.dp)
                                .align(Alignment.BottomCenter)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(3.dp, 0.dp)
                            ) {
                                ProvideTextStyle(
                                    MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                ) { it() }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        song.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = when {
                                highlighted || isCurrentPlaying -> MaterialTheme.colorScheme.primary
                                else -> LocalTextStyle.current.color
                            }
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    song.artistName?.let { artistName ->
                        Text(
                            artistName,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(15.dp))

                Row {
                    if (!disableHeartIcon && isFavorite) {
                        IconButton(
                            modifier = Modifier.offset(4.dp, 0.dp),
                            onClick = {
                                context.symphony.groove.playlist.unfavorite(song.id)
                            }
                        ) {
                            Icon(
                                Icons.Default.Favorite,
                                null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    var showOptionsMenu by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { showOptionsMenu = !showOptionsMenu }
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            null,
                            modifier = Modifier.size(24.dp),
                        )
                        SongDropdownMenu(
                            context,
                            song,
                            isFavorite = isFavorite,
                            trailingContent = trailingOptionsContent,
                            expanded = showOptionsMenu,
                            onDismissRequest = {
                                showOptionsMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SongDropdownMenu(
    context: ViewContext,
    song: Song,
    isFavorite: Boolean,
    trailingContent: (@Composable ColumnScope.(() -> Unit) -> Unit)? = null,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
) {
    var showInfoDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Default.Favorite, null)
            },
            text = {
                Text(
                    if (isFavorite) context.symphony.t.Unfavorite
                    else context.symphony.t.Favorite
                )
            },
            onClick = {
                onDismissRequest()
                context.symphony.groove.playlist.run {
                    when {
                        isFavorite -> unfavorite(song.id)
                        else -> favorite(song.id)
                    }
                }
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Default.PlaylistPlay, null)
            },
            text = {
                Text(context.symphony.t.PlayNext)
            },
            onClick = {
                onDismissRequest()
                context.symphony.radio.queue.add(
                    song,
                    context.symphony.radio.queue.currentSongIndex + 1
                )
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Default.PlaylistPlay, null)
            },
            text = {
                Text(context.symphony.t.AddToQueue)
            },
            onClick = {
                onDismissRequest()
                context.symphony.radio.queue.add(song)
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Default.PlaylistAdd, null)
            },
            text = {
                Text(context.symphony.t.AddToPlaylist)
            },
            onClick = {
                onDismissRequest()
                showAddToPlaylistDialog = true
            }
        )
        song.artistName?.let { artistName ->
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Default.Person, null)
                },
                text = {
                    Text(context.symphony.t.ViewArtist)
                },
                onClick = {
                    onDismissRequest()
                    context.navController.navigate(
                        RoutesBuilder.buildArtistRoute(artistName)
                    )
                }
            )
        }
        song.additional.albumArtist?.let { albumArtist ->
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Default.Person, null)
                },
                text = {
                    Text(context.symphony.t.ViewAlbumArtist)
                },
                onClick = {
                    onDismissRequest()
                    context.navController.navigate(
                        RoutesBuilder.buildAlbumArtistRoute(albumArtist)
                    )
                }
            )
        }
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Default.Album, null)
            },
            text = {
                Text(context.symphony.t.ViewAlbum)
            },
            onClick = {
                onDismissRequest()
                context.navController.navigate(
                    RoutesBuilder.buildAlbumRoute(song.albumId)
                )
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Default.Info, null)
            },
            text = {
                Text(context.symphony.t.Details)
            },
            onClick = {
                onDismissRequest()
                showInfoDialog = true
            }
        )
        trailingContent?.invoke(this, onDismissRequest)
    }

    if (showInfoDialog) {
        SongInformationDialog(
            context,
            song = song,
            onDismissRequest = {
                showInfoDialog = false
            }
        )
    }

    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            context,
            songIds = listOf(song.id),
            onDismissRequest = {
                showAddToPlaylistDialog = false
            }
        )
    }
}

