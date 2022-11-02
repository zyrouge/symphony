package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.Artist
import io.github.zyrouge.symphony.ui.components.*
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistView(context: ViewContext, artistName: String) {
    var artist by remember {
        mutableStateOf(context.symphony.groove.artist.getArtistFromName(artistName))
    }
    val songs = remember {
        mutableStateListOf(
            *context.symphony.groove.song.getSongsOfArtist(artistName).toTypedArray()
        )
    }
    var isViable by remember { mutableStateOf(artist != null) }

    EventerEffect(context.symphony.groove.artist.onUpdate) {
        artist = context.symphony.groove.artist.getArtistFromName(artistName)
        songs.clear()
        songs.addAll(context.symphony.groove.song.getSongsOfArtist(artistName))
        isViable = artist != null
    }

    EventerEffect(context.symphony.groove.song.onUpdate) {
        songs.clear()
        songs.addAll(context.symphony.groove.song.getSongsOfArtist(artistName))
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { context.navController.popBackStack() }
                    ) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                title = {
                    TopAppBarMinimalTitle {
                        Text(
                            context.symphony.t.artist
                                    + if (artist != null) " - $artistName" else ""
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
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
                        songs = songs,
                        leadingContent = {
                            item {
                                ArtistHero(context, artist!!)
                            }
                        }
                    )
                } else UnknownArtist(context, artistName)
            }
        },
        bottomBar = {
            NowPlayingBottomBar(context)
        }
    )
}

@Composable
private fun ArtistHero(context: ViewContext, artist: Artist) {
    val defaultHorizontalPadding = 20.dp
    BoxWithConstraints {
        Image(
            artist.getArtwork(context.symphony, 500).asImageBitmap(),
            null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(maxWidth.times(0.7f))
        )
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.Companion.verticalGradient(
                        0f to Color.Transparent,
                        1f to MaterialTheme.colorScheme.surface.copy(
                            alpha = 0.7f
                        )
                    )
                )
                .align(Alignment.BottomStart)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Box(
                modifier = Modifier
                    .padding(defaultHorizontalPadding, 32.dp, defaultHorizontalPadding, 12.dp)
                    .weight(1f)
            ) {
                Text(
                    artist.artistName,
                    style = MaterialTheme.typography.headlineSmall
                        .copy(fontWeight = FontWeight.Bold)
                )
            }

            Box(modifier = Modifier.padding(4.dp)) {
                var showOptionsMenu by remember {
                    mutableStateOf(false)
                }
                IconButton(
                    onClick = {
                        showOptionsMenu = !showOptionsMenu
                    }
                ) {
                    Icon(Icons.Default.MoreVert, null)
                    ArtistDropdownMenu(
                        context,
                        artist,
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

@Composable
private fun UnknownArtist(context: ViewContext, artistName: String) {
    IconTextBody(
        icon = { modifier ->
            Icon(
                Icons.Default.PriorityHigh,
                null,
                modifier = modifier
            )
        },
        content = {
            Text(context.symphony.t.unknownArtistX(artistName))
        }
    )
}
