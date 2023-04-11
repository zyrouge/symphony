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
import io.github.zyrouge.symphony.utils.asImmutableList
import io.github.zyrouge.symphony.utils.swap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumView(context: ViewContext, albumId: Long) {
    var album by remember {
        mutableStateOf(context.symphony.groove.album.getAlbumWithId(albumId))
    }
    val songsMutable = remember {
        context.symphony.groove.album.getSongsOfAlbumId(albumId).toMutableStateList()
    }
    val songs = songsMutable.asImmutableList()
    var isViable by remember { mutableStateOf(album != null) }

    EventerEffect(context.symphony.groove.album.onUpdate) {
        album = context.symphony.groove.album.getAlbumWithId(albumId)
        songsMutable.swap(context.symphony.groove.album.getSongsOfAlbumId(albumId))
        isViable = album != null
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
                        type = SongListType.Album,
                        leadingContent = {
                            item {
                                AlbumHero(context, album!!)
                            }
                        }
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
