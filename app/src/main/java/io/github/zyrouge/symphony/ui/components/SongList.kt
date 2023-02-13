package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import io.github.zyrouge.symphony.services.groove.GrooveKinds
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.services.groove.SongRepository
import io.github.zyrouge.symphony.services.groove.SongSortBy
import io.github.zyrouge.symphony.services.radio.Radio
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun SongList(
    context: ViewContext,
    songs: List<Song>,
    leadingContent: (LazyListScope.() -> Unit)? = null,
    trailingContent: (LazyListScope.() -> Unit)? = null,
    type: SongListType = SongListType.Default,
) {
    var sortBy by remember {
        mutableStateOf(type.getLastUsedSortBy(context))
    }
    var sortReverse by remember {
        mutableStateOf(type.getLastUsedSortReverse(context))
    }
    val sortedSongs by remember {
        derivedStateOf { SongRepository.sort(songs, sortBy, sortReverse) }
    }

    MediaSortBarScaffold(
        mediaSortBar = {
            MediaSortBar(
                context,
                reverse = sortReverse,
                onReverseChange = {
                    sortReverse = it
                    type.setLastUsedSortReverse(context, it)
                },
                sort = sortBy,
                sorts = SongSortBy.values().associateWith { x -> { x.label(it) } },
                onSortChange = {
                    sortBy = it
                    type.setLastUsedSortBy(context, it)
                },
                label = {
                    Text(context.symphony.t.XSongs(songs.size))
                },
                onShufflePlay = {
                    context.symphony.radio.shorty.playQueue(sortedSongs, shuffle = true)
                }
            )
        },
        content = {
            val lazyListState = rememberLazyListState()

            LazyColumn(
                state = lazyListState,
                modifier = Modifier.drawScrollBar(lazyListState)
            ) {
                leadingContent?.invoke(this)
                itemsIndexed(
                    sortedSongs,
                    key = { i, x -> "$i-${x.id}" },
                    contentType = { _, _ -> GrooveKinds.SONG }
                ) { i, song ->
                    SongCard(context, song) {
                        context.symphony.radio.shorty.playQueue(
                            sortedSongs,
                            Radio.PlayOptions(index = i)
                        )
                    }
                }
                trailingContent?.invoke(this)
            }
        }
    )
}

fun SongSortBy.label(context: ViewContext) = when (this) {
    SongSortBy.CUSTOM -> context.symphony.t.custom
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
    SongSortBy.TRACK_NUMBER -> context.symphony.t.trackNumber
}

enum class SongListType {
    Default,
    Playlist,
    Album;

    fun getLastUsedSortBy(context: ViewContext): SongSortBy {
        val sort = when (this) {
            Default -> context.symphony.settings.getLastUsedSongsSortBy()
            Playlist -> context.symphony.settings.getLastUsedPlaylistSongsSortBy()
            Album -> context.symphony.settings.getLastUsedAlbumSongsSortBy()
        }
        return sort ?: SongSortBy.TITLE
    }

    fun setLastUsedSortBy(context: ViewContext, sort: SongSortBy) {
        when (this) {
            Default -> context.symphony.settings.setLastUsedSongsSortBy(sort)
            Playlist -> context.symphony.settings.setLastUsedPlaylistSongsSortBy(sort)
            Album -> context.symphony.settings.setLastUsedAlbumSongsSortBy(sort)
        }
    }

    fun getLastUsedSortReverse(context: ViewContext): Boolean = when (this) {
        Default -> context.symphony.settings.getLastUsedSongsSortReverse()
        Playlist -> context.symphony.settings.getLastUsedPlaylistSongsSortReverse()
        Album -> context.symphony.settings.getLastUsedAlbumSongsSortReverse()
    }

    fun setLastUsedSortReverse(context: ViewContext, reverse: Boolean) {
        when (this) {
            Default -> context.symphony.settings.setLastUsedSongsSortReverse(reverse)
            Playlist -> context.symphony.settings.setLastUsedPlaylistSongsSortReverse(reverse)
            Album -> context.symphony.settings.setLastUsedAlbumSongsSortReverse(reverse)
        }
    }
}
