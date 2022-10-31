package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.services.groove.AlbumRepository
import io.github.zyrouge.symphony.services.groove.AlbumSortBy
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.swap
import kotlinx.coroutines.launch

@Composable
fun AlbumGrid(context: ViewContext, albums: List<Album>) {
    val scope = rememberCoroutineScope()
    var sortBy by remember {
        mutableStateOf(
            context.symphony.settings.getLastUsedAlbumsSortBy() ?: AlbumSortBy.ALBUM_NAME
        )
    }
    var sortReverse by remember {
        mutableStateOf(context.symphony.settings.getLastUsedAlbumsSortReverse())
    }
    val sortedAlbums = remember {
        mutableStateListOf<Album>().apply {
            swap(AlbumRepository.sort(albums, sortBy, sortReverse))
        }
    }

    fun sortAlbumsAgain() {
        sortedAlbums.swap(AlbumRepository.sort(albums, sortBy, sortReverse))
    }

    LaunchedEffect(LocalContext.current) {
        scope.launch {
            snapshotFlow { albums }.collect { sortAlbumsAgain() }
        }
    }

    ResponsiveGrid(
        topBar = {
            MediaSortBar(
                context,
                reverse = sortReverse,
                onReverseChange = {
                    sortReverse = it
                    sortAlbumsAgain()
                    context.symphony.settings.setLastUsedAlbumsSortReverse(it)
                },
                sort = sortBy,
                sorts = AlbumSortBy.values().associateWith { x -> { x.label(it) } },
                onSortChange = {
                    sortBy = it
                    sortAlbumsAgain()
                    context.symphony.settings.setLastUsedAlbumsSortBy(it)
                },
                label = {
                    Text(context.symphony.t.XAlbums(albums.size))
                }
            )
        },
        content = {
            items(sortedAlbums) { album ->
                AlbumTile(context, album)
            }
        }
    )
}

private fun AlbumSortBy.label(context: ViewContext) = when (this) {
    AlbumSortBy.ALBUM_NAME -> context.symphony.t.album
    AlbumSortBy.ARTIST_NAME -> context.symphony.t.artist
}
