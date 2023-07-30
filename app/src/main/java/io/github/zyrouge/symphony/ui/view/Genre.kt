package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.zyrouge.symphony.ui.components.*
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.nowPlaying.NowPlayingBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreView(context: ViewContext, genreId: String) {
    val allGenreIds = context.symphony.groove.genre.all
    val allSongIds = context.symphony.groove.song.all
    val genre by remember(allGenreIds) {
        derivedStateOf { context.symphony.groove.genre.get(genreId) }
    }
    val songIds by remember(genre, allSongIds) {
        derivedStateOf { context.symphony.groove.genre.getSongIds(genreId) }
    }
    val isViable by remember {
        derivedStateOf { allGenreIds.contains(genreId) }
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
                        Text("${context.symphony.t.Genre} - $genre")
                    }
                },
                actions = {
                    var showOptionsMenu by remember { mutableStateOf(false) }

                    IconButton(
                        onClick = {
                            showOptionsMenu = !showOptionsMenu
                        }
                    ) {
                        Icon(Icons.Default.MoreVert, null)
                        GenericSongListDropdown(
                            context,
                            songIds = songIds,
                            expanded = showOptionsMenu,
                            onDismissRequest = {
                                showOptionsMenu = false
                            }
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
                when {
                    isViable -> SongList(context, songIds = songIds)
                    else -> UnknownGenre(context, genreId)
                }
            }
        },
        bottomBar = {
            NowPlayingBottomBar(context)
        }
    )
}

@Composable
private fun UnknownGenre(context: ViewContext, genre: String) {
    IconTextBody(
        icon = { modifier ->
            Icon(
                Icons.Default.Tune,
                null,
                modifier = modifier
            )
        },
        content = {
            Text(context.symphony.t.UnknownGenreX(genre))
        }
    )
}
