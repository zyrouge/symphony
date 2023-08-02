package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.ui.components.*
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumView(context: ViewContext, albumId: Long) {
    val allAlbumIds = context.symphony.groove.album.all
    val allSongIds = context.symphony.groove.song.all
    val album by remember(allAlbumIds) {
        derivedStateOf { context.symphony.groove.album.get(albumId) }
    }
    val songIds by remember(album, allSongIds) {
        derivedStateOf { context.symphony.groove.album.getSongIds(albumId) }
    }
    val isViable by remember {
        derivedStateOf { allAlbumIds.contains(albumId) }
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
                        type = SongListType.Album,
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
                } else UnknownAlbum(context, albumId)
            }
        },
        bottomBar = {
            NowPlayingBottomBar(context)
        }
    )
}

@Composable
private fun AlbumHero(context: ViewContext, album: Album) {
    GenericGrooveBanner(
        image = album.createArtworkImageRequest(context.symphony).build(),
        options = { expanded, onDismissRequest ->
            AlbumDropdownMenu(
                context,
                album,
                expanded = expanded,
                onDismissRequest = onDismissRequest
            )
        },
        content = {
            Column {
                Text(album.name)
                album.artist?.let { artistName ->
                    Text(
                        artistName,
                        style = MaterialTheme.typography.bodyMedium
                            .copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    )
}

@Composable
private fun UnknownAlbum(context: ViewContext, albumId: Long) {
    IconTextBody(
        icon = { modifier ->
            Icon(
                Icons.Default.Album,
                null,
                modifier = modifier
            )
        },
        content = {
            Text(context.symphony.t.UnknownAlbumId(albumId.toString()))
        }
    )
}
