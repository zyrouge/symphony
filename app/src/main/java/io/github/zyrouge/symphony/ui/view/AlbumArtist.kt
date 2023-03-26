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
import io.github.zyrouge.symphony.services.groove.AlbumArtist
import io.github.zyrouge.symphony.ui.components.*
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.swap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumArtistView(context: ViewContext, artistName: String) {
    var albumArtist by remember {
        mutableStateOf(context.symphony.groove.albumArtist.getAlbumArtistFromArtistName(artistName))
    }
    val songs = remember {
        context.symphony.groove.albumArtist.getSongsOfAlbumArtist(artistName).toMutableStateList()
    }
    val albums = remember {
        context.symphony.groove.albumArtist.getAlbumsOfAlbumArtist(artistName).toMutableStateList()
    }
    var isViable by remember { mutableStateOf(albumArtist != null) }

    EventerEffect(context.symphony.groove.albumArtist.onUpdate) {
        albumArtist = context.symphony.groove.albumArtist.getAlbumArtistFromArtistName(artistName)
        songs.swap(context.symphony.groove.albumArtist.getSongsOfAlbumArtist(artistName))
        albums.swap(context.symphony.groove.albumArtist.getAlbumsOfAlbumArtist(artistName))
        isViable = albumArtist != null
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
                            context.symphony.t.AlbumArtist +
                                    (albumArtist?.let { " - ${it.name}" } ?: ""),
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
                                AlbumArtistHero(context, albumArtist!!)
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
                } else UnknownAlbumArtist(context, artistName)
            }
        },
        bottomBar = {
            NowPlayingBottomBar(context)
        }
    )
}

@Composable
private fun AlbumArtistHero(context: ViewContext, albumArtist: AlbumArtist) {
    GenericGrooveBanner(
        image = albumArtist.createArtworkImageRequest(context.symphony).build(),
        options = { expanded, onDismissRequest ->
            AlbumArtistDropdownMenu(
                context,
                albumArtist,
                expanded = expanded,
                onDismissRequest = onDismissRequest
            )
        },
        content = {
            Text(albumArtist.name)
        }
    )
}

@Composable
private fun UnknownAlbumArtist(context: ViewContext, artistName: String) {
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
