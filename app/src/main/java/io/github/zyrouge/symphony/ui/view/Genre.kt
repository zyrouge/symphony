package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.components.*
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.swap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreView(context: ViewContext, genre: String) {
    val songs = remember {
        mutableStateListOf<Song>().apply {
            swap(context.symphony.groove.song.getSongsOfGenre(genre))
        }
    }
    var isViable by remember { mutableStateOf(songs.isNotEmpty()) }

    val onGenreUpdate = {
        songs.swap(context.symphony.groove.song.getSongsOfGenre(genre))
        isViable = songs.isNotEmpty()
    }

    EventerEffect(context.symphony.groove.genre.onUpdate) { onGenreUpdate() }
    EventerEffect(context.symphony.groove.song.onUpdate) { onGenreUpdate() }

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
                        Text("${context.symphony.t.genre} - $genre")
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
                    isViable -> SongList(context, songs = songs)
                    else -> UnknownGenre(context, genre)
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
            Text(context.symphony.t.unknownGenreX(genre))
        }
    )
}
