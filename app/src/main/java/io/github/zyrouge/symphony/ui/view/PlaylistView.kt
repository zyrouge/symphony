package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.zyrouge.symphony.services.groove.repositories.PlaylistRepository
import io.github.zyrouge.symphony.ui.components.AnimatedNowPlayingBottomBar
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.components.PlaylistDropdownMenu
import io.github.zyrouge.symphony.ui.components.SongList
import io.github.zyrouge.symphony.ui.components.TopAppBarMinimalTitle
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.ThemeColors
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.transformLatest
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistViewRoute(val playlistId: String)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
@Composable
fun PlaylistView(context: ViewContext, route: PlaylistViewRoute) {
    val playlistFlow = context.symphony.groove.playlist.findByIdAsFlow(route.playlistId)
    val playlist by playlistFlow.collectAsStateWithLifecycle(null)
    val songsSortBy by context.symphony.settings.lastUsedPlaylistSongsSortBy.flow.collectAsStateWithLifecycle()
    val songsSortReverse by context.symphony.settings.lastUsedPlaylistSongsSortReverse.flow.collectAsStateWithLifecycle()
    val songsFlow = playlistFlow.transformLatest { playlist ->
        val value = when {
            playlist == null -> emptyFlow()
            else -> context.symphony.groove.playlist.findSongsByIdAsFlow(
                playlist.entity.id,
                songsSortBy,
                songsSortReverse,
            )
        }
        emitAll(value)
    }
    val songs by songsFlow.collectAsStateWithLifecycle(emptyList())
    var showOptionsMenu by remember { mutableStateOf(false) }

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
                        Text("${context.symphony.t.Playlist} - ${playlist?.entity?.title ?: context.symphony.t.UnknownSymbol}")
                    }
                },
                actions = {
                    if (playlist != null) {
                        IconButton(
                            onClick = {
                                showOptionsMenu = true
                            }
                        ) {
                            Icon(Icons.Filled.MoreVert, null)
                            PlaylistDropdownMenu(
                                context,
                                playlist = playlist!!,
                                songs = songs,
                                expanded = showOptionsMenu,
                                onDelete = {
                                    context.navController.popBackStack()
                                },
                                onDismissRequest = {
                                    showOptionsMenu = false
                                }
                            )
                        }
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
                    playlist != null -> SongList(
                        context,
                        songs = songs,
                        sortBy = songsSortBy,
                        sortReverse = songsSortReverse,
                        disableHeartIcon = playlist?.entity?.internalId == PlaylistRepository.PLAYLIST_INTERNAL_ID_FAVORITES,
                        trailingOptionsContent = { _, song, onDismissRequest ->
                            playlist
                                ?.takeIf { !it.entity.isModifiable }
                                ?.let {
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(
                                                Icons.Filled.DeleteForever,
                                                null,
                                                tint = ThemeColors.Red,
                                            )
                                        },
                                        text = {
                                            Text(context.symphony.t.RemoveFromPlaylist)
                                        },
                                        onClick = {
                                            onDismissRequest()
                                            context.symphony.groove.playlist.removeSongs(
                                                it.entity.id,
                                                listOf(song.id),
                                            )
                                        }
                                    )
                                }
                        },
                    )

                    else -> UnknownPlaylist(context, route.playlistId)
                }
            }
        },
        bottomBar = {
            AnimatedNowPlayingBottomBar(context)
        }
    )
}

@Composable
private fun UnknownPlaylist(context: ViewContext, playlistId: String) {
    IconTextBody(
        icon = { modifier ->
            Icon(
                Icons.AutoMirrored.Filled.QueueMusic,
                null,
                modifier = modifier
            )
        },
        content = {
            Text(context.symphony.t.UnknownPlaylistX(playlistId))
        }
    )
}
