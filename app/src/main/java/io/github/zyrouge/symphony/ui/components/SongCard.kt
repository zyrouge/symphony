package io.github.zyrouge.symphony.ui.components

import android.content.Intent
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import io.github.zyrouge.symphony.services.groove.entities.Album
import io.github.zyrouge.symphony.services.groove.entities.Artist
import io.github.zyrouge.symphony.services.groove.entities.Song
import io.github.zyrouge.symphony.services.groove.entities.SongQueue
import io.github.zyrouge.symphony.services.radio.RadioQueue
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.createGrooveArtworkImageRequest
import io.github.zyrouge.symphony.ui.view.AlbumViewRoute
import io.github.zyrouge.symphony.ui.view.ArtistViewRoute
import io.github.zyrouge.symphony.utils.Logger
import kotlinx.coroutines.launch

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
    val songQueue by context.symphony.radio.getQueueAsFlow().collectAsStateWithLifecycle(null)
    val isCurrentPlaying by remember(autoHighlight, song, songQueue) {
        derivedStateOf { autoHighlight && song.id == songQueue?.entity?.playingId }
    }
    val isFavorite by context.symphony.groove.playlist.isFavoriteSongAsFlow(song.id)
        .collectAsStateWithLifecycle(false)
    val artists by context.symphony.groove.song.findArtistsOfIdAsFlow(song.id)
        .collectAsStateWithLifecycle(emptyList())
    val albums by context.symphony.groove.song.findAlbumsOfIdAsFlow(song.id)
        .collectAsStateWithLifecycle(emptyList())
    val artworkUri by context.symphony.groove.song.getArtworkUriAsFlow(song.id)
        .collectAsStateWithLifecycle(null)

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
                        createGrooveArtworkImageRequest(context.symphony, artworkUri),
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
                    if (artists.isNotEmpty()) {
                        Text(
                            artists.joinToString { it.entity.name },
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
                                context.symphony.groove.coroutineScope.launch {
                                    context.symphony.groove.playlist.removeFromFavorites(song.id)
                                }
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
                            songQueue = songQueue?.entity,
                            song = song,
                            artists = artists.map { it.entity },
                            albums = albums.map { it.entity },
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
    songQueue: SongQueue?,
    song: Song,
    artists: List<Artist>,
    albums: List<Album>,
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
                Icon(Icons.Filled.Favorite, null)
            },
            text = {
                Text(
                    if (isFavorite) context.symphony.t.Unfavorite
                    else context.symphony.t.Favorite
                )
            },
            onClick = {
                onDismissRequest()
                context.symphony.groove.coroutineScope.launch {
                    when {
                        isFavorite -> context.symphony.groove.playlist.removeFromFavorites(song.id)
                        else -> context.symphony.groove.playlist.addToFavorites(song.id)
                    }
                }
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.AutoMirrored.Filled.PlaylistPlay, null)
            },
            text = {
                Text(context.symphony.t.PlayNext)
            },
            onClick = {
                onDismissRequest()
                context.symphony.groove.coroutineScope.launch {
                    context.symphony.radio.add(
                        song.id,
                        when {
                            songQueue?.playingId != null -> RadioQueue.AddPosition.After(songQueue.playingId)
                            else -> RadioQueue.AddPosition.AfterTail
                        },
                    )
                }
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.AutoMirrored.Filled.PlaylistPlay, null)
            },
            text = {
                Text(context.symphony.t.AddToQueue)
            },
            onClick = {
                onDismissRequest()
                context.symphony.groove.coroutineScope.launch {
                    context.symphony.radio.add(song.id)
                }
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null)
            },
            text = {
                Text(context.symphony.t.AddToPlaylist)
            },
            onClick = {
                onDismissRequest()
                showAddToPlaylistDialog = true
            }
        )
        artists.forEach {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Filled.Person, null)
                },
                text = {
                    Text("${context.symphony.t.ViewArtist}: ${it.name}")
                },
                onClick = {
                    onDismissRequest()
                    context.navController.navigate(ArtistViewRoute(it.id))
                }
            )
        }
        albums.forEach {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Filled.Album, null)
                },
                text = {
                    Text("${context.symphony.t.ViewAlbum}: ${it.name}")
                },
                onClick = {
                    onDismissRequest()
                    context.navController.navigate(AlbumViewRoute(it.id))
                }
            )
        }
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Filled.Share, null)
            },
            text = {
                Text(context.symphony.t.ShareSong)
            },
            onClick = {
                onDismissRequest()
                try {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        putExtra(Intent.EXTRA_STREAM, song.uri)
                        type = context.activity.contentResolver.getType(song.uri)
                    }
                    context.activity.startActivity(intent)
                } catch (err: Exception) {
                    Logger.error("SongCard", "share failed", err)
                    Toast.makeText(
                        context.activity,
                        context.symphony.t.ShareFailedX(err.localizedMessage ?: err.toString()),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Filled.Info, null)
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
            songs = listOf(song),
            onDismissRequest = {
                showAddToPlaylistDialog = false
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
