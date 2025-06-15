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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.zyrouge.symphony.services.groove.entities.Artist
import io.github.zyrouge.symphony.ui.components.AlbumRow
import io.github.zyrouge.symphony.ui.components.AnimatedNowPlayingBottomBar
import io.github.zyrouge.symphony.ui.components.ArtistDropdownMenu
import io.github.zyrouge.symphony.ui.components.GenericGrooveBanner
import io.github.zyrouge.symphony.ui.components.GenericGrooveBannerQuadImage
import io.github.zyrouge.symphony.ui.components.IconButtonPlaceholder
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.components.SongList
import io.github.zyrouge.symphony.ui.components.TopAppBarMinimalTitle
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.transformLatest
import kotlinx.serialization.Serializable

@Serializable
data class ArtistViewRoute(val artistId: String)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
@Composable
fun ArtistView(context: ViewContext, route: ArtistViewRoute) {
    val artistFlow = context.symphony.groove.artist.findByIdAsFlow(route.artistId)
    val artist by artistFlow.collectAsStateWithLifecycle(null)
    val albums by context.symphony.groove.artist.findAlbumsOfIdAsFlow(route.artistId)
        .collectAsStateWithLifecycle(emptyList())
    val songsSortBy by context.symphony.settings.lastUsedArtistSongsSortBy.flow.collectAsStateWithLifecycle()
    val songsSortReverse by context.symphony.settings.lastUsedArtistSongsSortReverse.flow.collectAsStateWithLifecycle()
    val songsFlow = artistFlow.transformLatest { artist ->
        val value = when {
            artist == null -> emptyFlow()
            else -> context.symphony.groove.artist.findSongsByIdAsFlow(
                artist.entity.id,
                songsSortBy,
                songsSortReverse,
            )
        }
        emitAll(value)
    }
    val songs by songsFlow.collectAsStateWithLifecycle(emptyList())

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
                            "${context.symphony.t.Artist} - ${artist?.entity?.name ?: context.symphony.t.UnknownSymbol}",
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
                when {
                    artist != null -> SongList(
                        context,
                        songs = songs,
                        sortBy = songsSortBy,
                        sortReverse = songsSortReverse,
                        leadingContent = {
                            item {
                                ArtistHero(context, artist!!)
                            }
                            if (albums.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    AlbumRow(context, albums)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    HorizontalDivider()
                                }
                            }
                        }
                    )

                    else -> UnknownArtist(context, route.artistId)
                }
            }
        },
        bottomBar = {
            AnimatedNowPlayingBottomBar(context)
        }
    )
}

@Composable
private fun ArtistHero(context: ViewContext, artist: Artist.AlongAttributes) {
    val artworks by context.symphony.groove.artist.getTop4ArtworkUriAsFlow(artist.entity.id)
        .collectAsStateWithLifecycle(emptyList())

    GenericGrooveBanner(
        image = { constraints ->
            GenericGrooveBannerQuadImage(context, artworks, constraints)
        },
        options = { expanded, onDismissRequest ->
            ArtistDropdownMenu(
                context,
                artist,
                expanded = expanded,
                onDismissRequest = onDismissRequest
            )
        },
        content = {
            Text(artist.entity.name)
        }
    )
}

@Composable
private fun UnknownArtist(context: ViewContext, artistId: String) {
    IconTextBody(
        icon = { modifier ->
            Icon(
                Icons.Filled.PriorityHigh,
                null,
                modifier = modifier
            )
        },
        content = {
            Text(context.symphony.t.UnknownArtistX(artistId))
        }
    )
}
