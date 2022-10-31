package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.services.groove.SongRepository
import io.github.zyrouge.symphony.services.groove.SongSortBy
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.swap
import kotlinx.coroutines.launch

@Composable
fun SongList(
    context: ViewContext,
    songs: List<Song>,
    leadingContent: (LazyListScope.() -> Unit)? = null,
    trailingContent: (LazyListScope.() -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    var sortBy by remember {
        mutableStateOf(
            context.symphony.settings.getLastUsedSongsSortBy() ?: SongSortBy.TITLE
        )
    }
    var sortReverse by remember {
        mutableStateOf(context.symphony.settings.getLastUsedSongsSortReverse())
    }
    val sortedSongs = remember {
        mutableStateListOf<Song>().apply {
            swap(SongRepository.sort(songs, sortBy, sortReverse))
        }
    }

    fun sortSongsAgain() {
        sortedSongs.swap(SongRepository.sort(songs, sortBy, sortReverse))
    }

    LaunchedEffect(LocalContext.current) {
        scope.launch {
            snapshotFlow { songs }.collect { sortSongsAgain() }
        }
    }

    LazyColumn {
        leadingContent?.invoke(this)
        item {
            MediaSortBar(
                context,
                reverse = sortReverse,
                onReverseChange = {
                    sortReverse = it
                    sortSongsAgain()
                    context.symphony.settings.setLastUsedSongsSortReverse(it)
                },
                sort = sortBy,
                sorts = SongSortBy.values().associateWith { x -> { x.label(it) } },
                onSortChange = {
                    sortBy = it
                    sortSongsAgain()
                    context.symphony.settings.setLastUsedSongsSortBy(it)
                },
                label = {
                    Text(context.symphony.t.XSongs(songs.size))
                }
            )
        }
        itemsIndexed(sortedSongs) { i, song ->
            SongCard(context, song) {
                context.symphony.player.stop()
                context.symphony.player.addToQueue(
                    sortedSongs.subList(i, sortedSongs.size).toList()
                )
            }
        }
        trailingContent?.invoke(this)
    }
}

private fun SongSortBy.label(context: ViewContext) = when (this) {
    SongSortBy.TITLE -> context.symphony.t.title
    SongSortBy.ARTIST -> context.symphony.t.artist
    SongSortBy.ALBUM -> context.symphony.t.album
    SongSortBy.DURATION -> context.symphony.t.duration
    SongSortBy.DATE_ADDED -> context.symphony.t.dateAdded
    SongSortBy.DATE_MODIFIED -> context.symphony.t.lastModified
    SongSortBy.COMPOSER -> context.symphony.t.composer
    SongSortBy.ALBUM_ARTIST -> context.symphony.t.albumArtist
    SongSortBy.YEAR -> context.symphony.t.year
    SongSortBy.FILENAME -> context.symphony.t.filename
}
