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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumArtistView(context: ViewContext, artistId: String) {
    val allAlbumArtistIds = context.symphony.groove.albumArtist.all
    val allSongIds = context.symphony.groove.song.all
    val allAlbumIds = context.symphony.groove.album.all
    val albumArtist by remember(allAlbumArtistIds) {
        derivedStateOf { context.symphony.groove.albumArtist.get(artistId) }
    }
    val songIds by remember(albumArtist, allSongIds) {
        derivedStateOf { context.symphony.groove.albumArtist.getSongIds(artistId) }
    }
    val albumIds by remember(albumArtist, allAlbumIds) {
        derivedStateOf { context.symphony.groove.albumArtist.getAlbumIds(artistId) }
    }
    val isViable by remember {
        derivedStateOf { allAlbumArtistIds.contains(artistId) }
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
                        songIds = songIds,
                        leadingContent = {
                            item {
                                AlbumArtistHero(context, albumArtist!!)
                            }
                            if (albumIds.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    AlbumRow(context, albumIds)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Divider()
                                }
                            }
                        }
                    )
                } else UnknownAlbumArtist(context, artistId)
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
