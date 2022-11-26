package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.helpers.RoutesBuilder
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.createHandyAsyncImageRequest
import io.github.zyrouge.symphony.utils.randomSubList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForYouView(context: ViewContext, data: HomeViewData) {
    when {
        data.songs.isNotEmpty() && data.albums.isNotEmpty() -> {
            val randomAlbums by remember {
                derivedStateOf { data.albums.randomSubList(6) }
            }
            val randomArtists by remember {
                derivedStateOf { data.artists.randomSubList(6) }
            }
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                Row(modifier = Modifier.padding(20.dp, 0.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        ForYouButton(
                            icon = Icons.Default.PlayArrow,
                            text = {
                                Text(context.symphony.t.playAll)
                            },
                            onClick = {
                                context.symphony.radio.shorty.playQueue(data.songs)
                            },
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        ForYouButton(
                            icon = Icons.Default.Shuffle,
                            text = {
                                Text(context.symphony.t.shufflePlay)
                            },
                            onClick = {
                                context.symphony.radio.shorty.playQueue(
                                    data.songs,
                                    shuffle = true,
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                SideHeading {
                    Text(context.symphony.t.randomAlbums)
                }
                Spacer(modifier = Modifier.height(12.dp))
                SixGrid(randomAlbums) { album ->
                    Card(
                        onClick = {
                            context.navController.navigate(
                                RoutesBuilder.buildAlbumRoute(album.albumId)
                            )
                        }
                    ) {
                        AsyncImage(
                            createHandyAsyncImageRequest(
                                LocalContext.current,
                                album.getArtworkUri(context.symphony),
                            ),
                            null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp)),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                SideHeading {
                    Text(context.symphony.t.randomArtists)
                }
                Spacer(modifier = Modifier.height(12.dp))
                SixGrid(randomArtists) { artist ->
                    Card(
                        onClick = {
                            context.navController.navigate(
                                RoutesBuilder.buildArtistRoute(artist.artistName)
                            )
                        }
                    ) {
                        AsyncImage(
                            createHandyAsyncImageRequest(
                                LocalContext.current,
                                artist.getArtworkUri(context.symphony),
                            ),
                            null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp)),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
        else -> IconTextBody(
            icon = { modifier ->
                Icon(
                    Icons.Default.MusicNote,
                    null,
                    modifier = modifier,
                )
            },
            content = { Text(context.symphony.t.damnThisIsSoEmpty) },
        )
    }
}

@Composable
private fun SideHeading(text: @Composable () -> Unit) {
    Box(modifier = Modifier.padding(20.dp, 0.dp)) {
        ProvideTextStyle(MaterialTheme.typography.titleLarge) {
            text()
        }
    }
}

@Composable
private fun ForYouButton(
    icon: ImageVector,
    text: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    ElevatedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            text()
        }
    }
}

@Composable
private fun <T> SixGrid(
    items: List<T>,
    content: @Composable (T) -> Unit,
) {
    val gap = 12.dp
    Row(
        modifier = Modifier.padding(20.dp, 0.dp),
        horizontalArrangement = Arrangement.spacedBy(gap),
    ) {
        items.subList(0, 3).forEach {
            Box(modifier = Modifier.weight(1f)) {
                content(it)
            }
        }
    }
    Spacer(modifier = Modifier.height(gap))
    Row(
        modifier = Modifier.padding(20.dp, 0.dp),
        horizontalArrangement = Arrangement.spacedBy(gap),
    ) {
        items.subList(3, 6).forEach {
            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.width(maxWidth)) {
                    content(it)
                }
            }
        }
    }
}
