package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.Artist
import io.github.zyrouge.symphony.ui.components.*
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.asImmutableList
import io.github.zyrouge.symphony.utils.swap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistView(context: ViewContext, artistName: String) {
    var artist by remember {
        mutableStateOf(context.symphony.groove.artist.getArtistFromArtistName(artistName))
    }
    val songsMutable = remember {
        context.symphony.groove.artist.getSongsOfArtistName(artistName).toMutableStateList()
    }
    val songs = songsMutable.asImmutableList()
    val albumsMutable = remember {
        context.symphony.groove.artist.getAlbumsOfArtistName(artistName).toMutableStateList()
    }
    val albums = albumsMutable.asImmutableList()
    var isViable by remember { mutableStateOf(artist != null) }

    EventerEffect(context.symphony.groove.artist.onUpdate) {
        artist = context.symphony.groove.artist.getArtistFromArtistName(artistName)
        songsMutable.swap(context.symphony.groove.artist.getSongsOfArtistName(artistName))
        albumsMutable.swap(context.symphony.groove.artist.getAlbumsOfArtistName(artistName))
        isViable = artist != null
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
                            context.symphony.t.Artist + (artist?.let { " - ${it.name}" } ?: ""),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
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
                            if (albums.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    AlbumRow(context, albums)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Divider()
                                }
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
    GenericGrooveBanner(
        image = artist.createArtworkImageRequest(context.symphony).build(),
        options = { expanded, onDismissRequest ->
            ArtistDropdownMenu(
                context,
                artist,
                expanded = expanded,
                onDismissRequest = onDismissRequest
            )
        },
        content = {
            Text(artist.name)
        }
    )
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
            Text(context.symphony.t.UnknownArtistX(artistName))
        }
    )
}
