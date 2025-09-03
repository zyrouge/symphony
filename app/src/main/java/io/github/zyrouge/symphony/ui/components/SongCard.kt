package io.github.zyrouge.symphony.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.AlbumArtistViewRoute
import io.github.zyrouge.symphony.ui.view.AlbumViewRoute
import io.github.zyrouge.symphony.ui.view.ArtistViewRoute

@Composable
fun SongCard(
    context: ViewContext,
    song: Song,
    highlighted: Boolean = false,
    autoHighlight: Boolean = true,
    disableHeartIcon: Boolean = false,
    leading: @Composable () -> Unit = {},
    thumbnailLabel: (@Composable () -> Unit)? = null,
    thumbnailLabelStyle: SongCardThumbnailLabelStyle = SongCardThumbnailLabelStyle.Default,
    trailingOptionsContent: (@Composable ColumnScope.(() -> Unit) -> Unit)? = null,
    onClick: () -> Unit,
) {
    val queue by context.symphony.radio.observatory.queue.collectAsState()
    val queueIndex by context.symphony.radio.observatory.queueIndex.collectAsState()
    val isCurrentPlaying by remember(autoHighlight, song, queue) {
        derivedStateOf { autoHighlight && song.id == queue.getOrNull(queueIndex) }
    }
    val favoriteSongIds by context.symphony.groove.playlist.favorites.collectAsState()
    val isFavorite by remember(favoriteSongIds, song) {
        derivedStateOf { favoriteSongIds.contains(song.id) }
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
                        val backgroundColor =
                            thumbnailLabelStyle.backgroundColor(MaterialTheme.colorScheme)
                        val contentColor =
                            thumbnailLabelStyle.contentColor(MaterialTheme.colorScheme)

                        Box(
                            modifier = Modifier
                                .offset(y = 8.dp)
                                .align(Alignment.BottomCenter)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        backgroundColor,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(3.dp, 0.dp)
                            ) {
                                ProvideTextStyle(
                                    MaterialTheme.typography.labelSmall.copy(
                                        color = contentColor
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
                    if (song.artists.isNotEmpty()) {
                        Text(
                            song.artists.joinToString(),
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
                                Icons.Filled.Favorite,
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
                            Icons.Filled.MoreVert,
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

class SongDropdownMenuData(
    val context: ViewContext,
    val song: Song,
    val isFavorite: Boolean,
    val expanded: Boolean,
    val onDismissRequest: () -> Unit,
) {
    var showInfoDialog by mutableStateOf(false)
    var showAddToPlaylistDialog by mutableStateOf(false)
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
    val data by remember {
        mutableStateOf(
            SongDropdownMenuData(
                context,
                song,
                isFavorite,
                expanded,
                onDismissRequest
            )
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        context.symphony.settings.songContextMenuActions.value.map {
            it.entry.invoke(data)
        }
        trailingContent?.invoke(this, onDismissRequest)
    }

    if (data.showInfoDialog) {
        SongInformationDialog(
            context,
            song = song,
            onDismissRequest = {
                data.showInfoDialog = false
            }
        )
    }

    if (data.showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            context,
            songIds = listOf(song.id),
            onDismissRequest = {
                data.showAddToPlaylistDialog = false
            }
        )
    }
}

enum class SongCardThumbnailLabelStyle {
    Default,
    Subtle,
}

private fun SongCardThumbnailLabelStyle.backgroundColor(colorScheme: ColorScheme) = when (this) {
    SongCardThumbnailLabelStyle.Default -> colorScheme.surfaceVariant
    SongCardThumbnailLabelStyle.Subtle -> colorScheme.surfaceVariant
}

private fun SongCardThumbnailLabelStyle.contentColor(colorScheme: ColorScheme) = when (this) {
    SongCardThumbnailLabelStyle.Default -> colorScheme.primary
    SongCardThumbnailLabelStyle.Subtle -> colorScheme.onSurfaceVariant
}

enum class SongContextMenuActions(
    var label: (context: ViewContext) -> String,
    var entry: @Composable (data: SongDropdownMenuData) -> Unit,
) {
    FAVORITE(
        label = { it.symphony.t.Favorite },
        entry = {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Filled.Favorite, null)
                },
                text = {
                    Text(
                        if (it.isFavorite) it.context.symphony.t.Unfavorite
                        else it.context.symphony.t.Favorite
                    )
                },
                onClick = {
                    it.onDismissRequest()
                    it.context.symphony.groove.playlist.run {
                        when {
                            it.isFavorite -> unfavorite(it.song.id)
                            else -> favorite(it.song.id)
                        }
                    }
                }
            )
        }
    ),
    PLAY_NEXT(
        label = { it.symphony.t.PlayNext },
        entry = {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.AutoMirrored.Filled.PlaylistPlay, null)
                },
                text = {
                    Text(it.context.symphony.t.PlayNext)
                },
                onClick = {
                    it.onDismissRequest()
                    it.context.symphony.radio.queue.add(
                        it.song.id,
                        it.context.symphony.radio.queue.currentSongIndex + 1
                    )
                }
            )
        }
    ),
    ADD_TO_QUEUE(
        label = { it.symphony.t.AddToQueue },
        entry = {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.AutoMirrored.Filled.PlaylistPlay, null)
                },
                text = {
                    Text(it.context.symphony.t.AddToQueue)
                },
                onClick = {
                    it.onDismissRequest()
                    it.context.symphony.radio.queue.add(it.song.id)
                }
            )
        }
    ),
    ADD_TO_PLAYLIST(
        label = { it.symphony.t.AddToPlaylist },
        entry = {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null)
                },
                text = {
                    Text(it.context.symphony.t.AddToPlaylist)
                },
                onClick = {
                    it.onDismissRequest()
                    it.showAddToPlaylistDialog = true
                }
            )
        }
    ),
    VIEW_ARTIST(
        label = { it.symphony.t.ViewArtist },
        entry = {
            it.song.artists.forEach { artistName ->
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(Icons.Filled.Person, null)
                    },
                    text = {
                        Text("${it.context.symphony.t.ViewArtist}: $artistName")
                    },
                    onClick = {
                        it.onDismissRequest()
                        it.context.navController.navigate(ArtistViewRoute(artistName))
                    }
                )
            }
        }
    ),
    VIEW_ALBUM_ARTIST(
        label = { it.symphony.t.ViewAlbumArtist },
        entry = {
            it.song.albumArtists.forEach { albumArtist ->
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(Icons.Filled.Person, null)
                    },
                    text = {
                        Text("${it.context.symphony.t.ViewAlbumArtist}: $albumArtist")
                    },
                    onClick = {
                        it.onDismissRequest()
                        it.context.navController.navigate(
                            AlbumArtistViewRoute(albumArtist)
                        )
                    }
                )
            }
        }
    ),
    VIEW_ALBUM(
        label = { it.symphony.t.ViewAlbum },
        entry = {
            it.context.symphony.groove.album.getIdFromSong(it.song)?.let { albumId ->
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(Icons.Filled.Album, null)
                    },
                    text = {
                        Text(it.context.symphony.t.ViewAlbum)
                    },
                    onClick = {
                        it.onDismissRequest()
                        it.context.navController.navigate(
                            AlbumViewRoute(albumId)
                        )
                    }
                )
            }
        }
    ),
    SHARE_SONG(
        label = { it.symphony.t.ShareSong },
        entry = {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Filled.Share, null)
                },
                text = {
                    Text(it.context.symphony.t.ShareSong)
                },
                onClick = {
                    it.onDismissRequest()
                    try {
                        val intent =
                            android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                putExtra(android.content.Intent.EXTRA_STREAM, it.song.uri)
                                type = it.context.activity.contentResolver.getType(it.song.uri)
                            }
                        it.context.activity.startActivity(intent)
                    } catch (err: Exception) {
                        io.github.zyrouge.symphony.utils.Logger.error(
                            "SongCard",
                            "share failed",
                            err
                        )
                        Toast.makeText(
                            it.context.activity,
                            it.context.symphony.t.ShareFailedX(
                                err.localizedMessage ?: err.toString()
                            ),
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            )
        }
    ),
    DETAILS(
        label = { it.symphony.t.Details },
        entry = {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Filled.Info, null)
                },
                text = {
                    Text(it.context.symphony.t.Details)
                },
                onClick = {
                    it.onDismissRequest()
                    it.showInfoDialog = true
                }
            )
        }
    )
}