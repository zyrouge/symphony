package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.Artist
import io.github.zyrouge.symphony.ui.components.AlbumRow
import io.github.zyrouge.symphony.ui.components.AnimatedNowPlayingBottomBar
import io.github.zyrouge.symphony.ui.components.ArtistDropdownMenu
import io.github.zyrouge.symphony.ui.components.GenericGrooveBanner
import io.github.zyrouge.symphony.ui.components.IconButtonPlaceholder
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.components.SongList
import io.github.zyrouge.symphony.ui.components.TopAppBarMinimalTitle
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistView(context: ViewContext, artistName: String) {
    val allArtistNames by context.symphony.groove.artist.all.collectAsState()
    val allSongIds by context.symphony.groove.song.all.collectAsState()
    val allAlbumIds by context.symphony.groove.album.all.collectAsState()
    val artist by remember(allArtistNames) {
        derivedStateOf { context.symphony.groove.artist.get(artistName) }
    }
    val songIds by remember(artist, allSongIds) {
        derivedStateOf { artist?.getSongIds(context.symphony) ?: listOf() }
    }
    val albumIds by remember(artist, allAlbumIds) {
        derivedStateOf { artist?.getAlbumIds(context.symphony) ?: listOf() }
    }
    val isViable by remember(allArtistNames) {
        derivedStateOf { allArtistNames.contains(artistName) }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { context.navController.popBackStack() }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
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
                        leadingContent = {
                            item {
                                ArtistHero(context, artist!!)
                            }
                            if (albumIds.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    AlbumRow(context, albumIds)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    HorizontalDivider()
                                }
                            }
                        }
                    )
                } else UnknownArtist(context, artistName)
            }
        },
        bottomBar = {
            AnimatedNowPlayingBottomBar(context)
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
                Icons.Filled.PriorityHigh,
                null,
                modifier = modifier
            )
        },
        content = {
            Text(context.symphony.t.UnknownArtistX(artistName))
        }
    )
}
