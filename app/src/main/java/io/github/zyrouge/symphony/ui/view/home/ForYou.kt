package io.github.zyrouge.symphony.ui.view.home

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import io.github.zyrouge.symphony.services.groove.repositories.SongRepository
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.createGrooveArtworkImageRequest
import io.github.zyrouge.symphony.utils.builtin.subListNonStrict
import kotlinx.coroutines.launch

enum class ForYou(val label: (context: ViewContext) -> String) {
    Albums(label = { it.symphony.t.SuggestedAlbums }),
    Artists(label = { it.symphony.t.SuggestedArtists }),
    AlbumArtists(label = { it.symphony.t.SuggestedAlbumArtists })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForYouView(context: ViewContext) {
    val libraryIsUpdating by context.symphony.groove.exposer.isUpdating.collectAsStateWithLifecycle()
    val recentlyAddedSongs by context.symphony.groove.song.valuesAsFlow(
        SongRepository.SortBy.DATE_MODIFIED,
        true,
        6
    ).collectAsStateWithLifecycle(emptyList())

    when {
        recentlyAddedSongs.isNotEmpty() -> {
//            val randomAlbums by remember(albumsIsUpdating, albumIds) {
//                derivedStateOf {
//                    runIfOrDefault(!albumsIsUpdating, listOf()) {
//                        albumIds.randomSubList(6)
//                    }
//                }
//            }
//            val randomArtists by remember(artistsIsUpdating, artistNames) {
//                derivedStateOf {
//                    runIfOrDefault(!artistsIsUpdating, listOf()) {
//                        artistNames.randomSubList(6)
//                    }
//                }
//            }
//            val randomAlbumArtists by remember(albumArtistsIsUpdating, albumArtistNames) {
//                derivedStateOf {
//                    runIfOrDefault(!albumArtistsIsUpdating, listOf()) {
//                        albumArtistNames.randomSubList(6)
//                    }
//                }
//            }

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Row(modifier = Modifier.padding(20.dp, 0.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        ForYouButton(
                            icon = Icons.Filled.PlayArrow,
                            text = {
                                Text(context.symphony.t.PlayAll)
                            },
                            enabled = !libraryIsUpdating,
                            onClick = {
                                context.symphony.groove.coroutineScope.launch {
                                    context.symphony.radio.clear()
                                    context.symphony.radio.add(recentlyAddedSongs)
                                    context.symphony.radio.play()
                                }
                            },
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        ForYouButton(
                            icon = Icons.Filled.Shuffle,
                            text = {
                                Text(context.symphony.t.ShufflePlay)
                            },
                            enabled = !libraryIsUpdating,
                            onClick = {
                                context.symphony.groove.coroutineScope.launch {
                                    context.symphony.radio.clear()
                                    context.symphony.radio.add(recentlyAddedSongs)
                                    context.symphony.radio.setShuffleMode(true)
                                    context.symphony.radio.play()
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                SideHeading {
                    Text(context.symphony.t.RecentlyAddedSongs)
                }
                Spacer(modifier = Modifier.height(12.dp))
                when {
                    recentlyAddedSongs.isEmpty() -> SixGridEmpty(context)
                    else -> BoxWithConstraints {
                        val tileWidth = this@BoxWithConstraints.maxWidth.times(0.7f)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Spacer(modifier = Modifier.width(12.dp))
                            recentlyAddedSongs.subListNonStrict(5).forEach { song ->
                                val tileHeight = 96.dp
                                val backgroundColor = MaterialTheme.colorScheme.surface
                                val artworkUri by context.symphony.groove.song.getArtworkUriAsFlow(
                                    song.id
                                ).collectAsStateWithLifecycle(null as Uri?)

                                ElevatedCard(
                                    modifier = Modifier
                                        .width(tileWidth)
                                        .height(tileHeight),
                                    onClick = {
                                        context.symphony.groove.coroutineScope.launch {
                                            context.symphony.radio.clear()
                                            context.symphony.radio.add(recentlyAddedSongs)
                                            context.symphony.radio.playBySongId(song.id)
                                        }
                                    }
                                ) {
                                    Box {
                                        AsyncImage(
                                            createGrooveArtworkImageRequest(
                                                context.symphony,
                                                artworkUri
                                            ),
                                            null,
                                            contentScale = ContentScale.FillWidth,
                                            modifier = Modifier.matchParentSize(),
                                        )
                                        Box(
                                            modifier = Modifier
                                                .matchParentSize()
                                                .background(
                                                    Brush.horizontalGradient(
                                                        colors = listOf(
                                                            backgroundColor.copy(alpha = 0.2f),
                                                            backgroundColor.copy(alpha = 0.7f),
                                                            backgroundColor.copy(alpha = 0.8f),
                                                        ),
                                                    )
                                                )
                                        )
                                        Row(modifier = Modifier.padding(8.dp)) {
                                            Box {
                                                AsyncImage(
                                                    createGrooveArtworkImageRequest(
                                                        context.symphony,
                                                        artworkUri
                                                    ),
                                                    null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .aspectRatio(1f)
                                                        .fillMaxHeight()
                                                        .clip(RoundedCornerShape(4.dp)),
                                                )
                                                Box(
                                                    modifier = Modifier.matchParentSize(),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .background(
                                                                backgroundColor.copy(alpha = 0.25f),
                                                                CircleShape,
                                                            )
                                                            .padding(1.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Filled.PlayArrow,
                                                            null,
                                                            modifier = Modifier.size(20.dp),
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column(
                                                modifier = Modifier.fillMaxSize(),
                                                verticalArrangement = Arrangement.Center,
                                            ) {
                                                Text(
                                                    song.title,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                                if (song.artists.isNotEmpty()) {
                                                    Text(
                                                        song.artists.joinToString(),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                    }
                }
                /* // suggested content skipped — data derivation was removed in refactoring
                val contents by context.symphony.settings.forYouContents.flow.collectAsStateWithLifecycle()
                contents.forEach {
                    when (it) {
                        ForYou.Albums -> SuggestedAlbums(
                            context,
                            isLoading = albumsIsUpdating,
                            albumIds = randomAlbums,
                        )

                        ForYou.Artists -> SuggestedArtists(
                            context,
                            label = context.symphony.t.SuggestedArtists,
                            isLoading = artistsIsUpdating,
                            artistNames = randomArtists,
                        )

                        ForYou.AlbumArtists -> SuggestedAlbumArtists(
                            context,
                            label = context.symphony.t.SuggestedAlbumArtists,
                            isLoading = albumArtistsIsUpdating,
                            albumArtistNames = randomAlbumArtists,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                */
            }
        }

        else -> IconTextBody(
            icon = { modifier ->
                Icon(
                    Icons.Filled.MusicNote,
                    null,
                    modifier = modifier,
                )
            },
            content = { Text(context.symphony.t.DamnThisIsSoEmpty) },
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
    enabled: Boolean,
    onClick: () -> Unit,
) {
    ElevatedButton(
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
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

/* // SixGridLoading — unused after suggested content removal
@Composable
private fun SixGridLoading() {
    Box(
        modifier = Modifier
            .height((LocalConfiguration.current.screenHeightDp * 0.2f).dp)
            .fillMaxWidth()
            .padding(0.dp, 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

*/

@Composable
private fun SixGridEmpty(context: ViewContext) {
    val height = (LocalConfiguration.current.screenHeightDp * 0.15f).dp
    Box(
        modifier = Modifier
            .height(height)
            .fillMaxWidth()
            .padding(0.dp, 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            context.symphony.t.DamnThisIsSoEmpty,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        )
    }
}

/* // StatedSixGrid, SixGrid, Suggested* — unused after suggested content removal
@Composable
private fun <T> StatedSixGrid(
    context: ViewContext,
    isLoading: Boolean,
    items: List<T>,
    content: @Composable (T) -> Unit,
) {
    when {
        isLoading -> SixGridLoading()
        items.isEmpty() -> SixGridEmpty(context)
        else -> SixGrid(items) {
            content(it)
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
        (0..2).forEach { i ->
            val item = items.getOrNull(i)
            Box(modifier = Modifier.weight(1f)) {
                item?.let { content(it) }
            }
        }
    }
    if (items.size > 3) {
        Spacer(modifier = Modifier.height(gap))
        Row(
            modifier = Modifier.padding(20.dp, 0.dp),
            horizontalArrangement = Arrangement.spacedBy(gap),
        ) {
            (3..5).forEach { i ->
                val item = items.getOrNull(i)
                Box(modifier = Modifier.weight(1f)) {
                    item?.let { content(it) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuggestedAlbums(
    context: ViewContext,
    isLoading: Boolean,
    albums: List<Album.AlongAttributes>,
) {
    Spacer(modifier = Modifier.height(24.dp))
    SideHeading {
        Text(context.symphony.t.SuggestedAlbums)
    }
    Spacer(modifier = Modifier.height(12.dp))
    StatedSixGrid(context, isLoading, albums) { album ->
        Card(
            onClick = {
                context.navController.navigate(AlbumViewRoute(album.id))
            }
        ) {
            AsyncImage(
                album.createArtworkImageRequest(context.symphony.groove.album.getTop4ArtworkUriAsFlow())
                    .build(),
                null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp)),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuggestedArtists(
    context: ViewContext,
    label: String,
    isLoading: Boolean,
    artists: List<Artist.AlongAttributes>,
) {
    Spacer(modifier = Modifier.height(24.dp))
    SideHeading {
        Text(label)
    }
    Spacer(modifier = Modifier.height(12.dp))
    StatedSixGrid(context, isLoading, artists) { artist ->
        Card(
            onClick = {
                context.navController.navigate(ArtistViewRoute(artist.name))
            }
        ) {
            AsyncImage(
                artist.createArtworkImageRequest(context.symphony).build(),
                null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp)),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuggestedAlbumArtists(
    context: ViewContext,
    label: String,
    isLoading: Boolean,
    artists: List<Artist.AlongAttributes>,
) {
    Spacer(modifier = Modifier.height(24.dp))
    SideHeading {
        Text(label)
    }
    Spacer(modifier = Modifier.height(12.dp))
    StatedSixGrid(context, isLoading, artists) { artist ->
        Card(
            onClick = {
                context.navController.navigate(ArtistViewRoute(artist.name))
            }
        ) {
            AsyncImage(
                artist.createArtworkImageRequest(context.symphony).build(),
                null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp)),
            )
        }
    }
}
*/
