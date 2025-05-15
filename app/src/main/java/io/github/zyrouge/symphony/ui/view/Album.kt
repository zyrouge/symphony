package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.ui.components.AlbumDropdownMenu
import io.github.zyrouge.symphony.ui.components.AnimatedNowPlayingBottomBar
import io.github.zyrouge.symphony.ui.components.GenericGrooveBanner
import io.github.zyrouge.symphony.ui.components.IconButtonPlaceholder
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.components.SongCardThumbnailLabelStyle
import io.github.zyrouge.symphony.ui.components.SongList
import io.github.zyrouge.symphony.ui.components.SongListType
import io.github.zyrouge.symphony.ui.components.TopAppBarMinimalTitle
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import kotlinx.serialization.Serializable

@Serializable
data class AlbumViewRoute(val albumId: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumView(context: ViewContext, route: AlbumViewRoute) {
    val allAlbumIds by context.symphony.groove.album.all.collectAsState()
    val allSongIds by context.symphony.groove.song.all.collectAsState()
    val album by remember(allAlbumIds) {
        derivedStateOf { context.symphony.groove.album.get(route.albumId) }
    }
    val songIds by remember(album, allSongIds) {
        derivedStateOf { album?.getSongIds(context.symphony) ?: listOf() }
    }
    val isViable by remember(allAlbumIds) {
        derivedStateOf { allAlbumIds.contains(route.albumId) }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = { context.navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                title = {
                    TopAppBarMinimalTitle {
                        Text(
                            context.symphony.t.Album + (album?.let { " - ${it.name}" } ?: ""),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    IconButtonPlaceholder()
                },
            )
        },
        content = { contentPadding ->
            Box(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
            ) {
                if (isViable) {
                    SongList(
                        context,
                        songIds = songIds,
                        type = SongListType.Album(route.albumId),
                        leadingContent = {
                            item {
                                AlbumHero(context, album!!)
                            }
                        },
                        cardThumbnailLabel = { _, song ->
                            Text(song.trackNumber?.toString() ?: context.symphony.t.UnknownSymbol)
                        },
                        cardThumbnailLabelStyle = SongCardThumbnailLabelStyle.Subtle,
                    )
                } else UnknownAlbum(context, route.albumId)
            }
        },
        bottomBar = {
            AnimatedNowPlayingBottomBar(context)
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AlbumHero(context: ViewContext, album: Album) {
    GenericGrooveBanner(
        image = album.createArtworkImageRequest(context.symphony).build(),
        options = { expanded, onDismissRequest ->
            AlbumDropdownMenu(
                context,
                album,
                expanded = expanded,
                onDismissRequest = onDismissRequest,
            )
        },
        content = {
            Column {
                Text(album.name)
                if (album.artists.isNotEmpty()) {
                    ProvideTextStyle(MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)) {
                        FlowRow {
                            album.artists.forEachIndexed { i, it ->
                                Text(
                                    it,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.pointerInput(Unit) {
                                        detectTapGestures { _ ->
                                            context.navController.navigate(ArtistViewRoute(it))
                                        }
                                    },
                                )
                                if (i != album.artists.size - 1) {
                                    Text(", ")
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    album.startYear?.let { startYear ->
                        val endYear = album.endYear

                        Text(
                            when {
                                endYear == null || startYear == endYear -> startYear.toString()
                                else -> "$startYear - $endYear"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        CircleSeparator()
                    }
                    Text(
                        album.duration.toString(),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        })
}

@Composable
private fun UnknownAlbum(context: ViewContext, albumId: String) {
    IconTextBody(
        icon = { modifier ->
            Icon(Icons.Filled.Album, null, modifier = modifier)
        },
        content = {
            Text(context.symphony.t.UnknownAlbumX(albumId))
        }
    )
}

@Composable
private fun CircleSeparator() {
    val color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)

    Canvas(modifier = Modifier.size(4.dp)) {
        drawCircle(color)
    }
}