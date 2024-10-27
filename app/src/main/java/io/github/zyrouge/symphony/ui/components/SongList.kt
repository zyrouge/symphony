package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.zyrouge.symphony.services.groove.Groove
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.services.groove.repositories.SongRepository
import io.github.zyrouge.symphony.services.radio.Radio
import io.github.zyrouge.symphony.ui.helpers.ViewContext

enum class SongListType {
    Default,
    Playlist,
    Album,
}

@Composable
fun SongList(
    context: ViewContext,
    songIds: List<String>,
    songsCount: Int? = null,
    leadingContent: (LazyListScope.() -> Unit)? = null,
    trailingContent: (LazyListScope.() -> Unit)? = null,
    trailingOptionsContent: (@Composable ColumnScope.(Int, Song, () -> Unit) -> Unit)? = null,
    cardThumbnailLabel: (@Composable (Int, Song) -> Unit)? = null,
    cardThumbnailLabelStyle: SongCardThumbnailLabelStyle = SongCardThumbnailLabelStyle.Default,
    type: SongListType = SongListType.Default,
    disableHeartIcon: Boolean = false,
) {
    val sortBy by type.getLastUsedSortBy(context).collectAsState()
    val sortReverse by type.getLastUsedSortReverse(context).collectAsState()
    val sortedSongIds by remember(songIds, sortBy, sortReverse) {
        derivedStateOf {
            context.symphony.groove.song.sort(songIds, sortBy, sortReverse)
        }
    }

    MediaSortBarScaffold(
        mediaSortBar = {
            MediaSortBar(
                context,
                reverse = sortReverse,
                onReverseChange = {
                    type.setLastUsedSortReverse(context, it)
                },
                sort = sortBy,
                sorts = SongRepository.SortBy.entries
                    .associateWith { x -> ViewContext.parameterizedFn { x.label(it) } },
                onSortChange = {
                    type.setLastUsedSortBy(context, it)
                },
                label = {
                    Text(context.symphony.t.XSongs((songsCount ?: songIds.size).toString()))
                },
                onShufflePlay = {
                    context.symphony.radio.shorty.playQueue(sortedSongIds, shuffle = true)
                }
            )
        },
        content = {
            when {
                songIds.isEmpty() -> IconTextBody(
                    icon = { modifier ->
                        Icon(
                            Icons.Filled.MusicNote,
                            null,
                            modifier = modifier,
                        )
                    },
                    content = { Text(context.symphony.t.DamnThisIsSoEmpty) }
                )

                else -> {
                    val lazyListState = rememberLazyListState()

                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.drawScrollBar(lazyListState)
                    ) {
                        leadingContent?.invoke(this)
                        itemsIndexed(
                            sortedSongIds,
                            key = { i, x -> "$i-$x" },
                            contentType = { _, _ -> Groove.Kinds.SONG }
                        ) { i, songId ->
                            context.symphony.groove.song.get(songId)?.let { song ->
                                SongCard(
                                    context,
                                    song = song,
                                    thumbnailLabel = cardThumbnailLabel?.let {
                                        { it(i, song) }
                                    },
                                    thumbnailLabelStyle = cardThumbnailLabelStyle,
                                    disableHeartIcon = disableHeartIcon,
                                    trailingOptionsContent = trailingOptionsContent?.let {
                                        { onDismissRequest -> it(i, song, onDismissRequest) }
                                    },
                                ) {
                                    context.symphony.radio.shorty.playQueue(
                                        sortedSongIds,
                                        Radio.PlayOptions(index = i)
                                    )
                                }
                            }
                        }
                        trailingContent?.invoke(this)
                    }
                }
            }
        }
    )
}

fun SongRepository.SortBy.label(context: ViewContext) = when (this) {
    SongRepository.SortBy.CUSTOM -> context.symphony.t.Custom
    SongRepository.SortBy.TITLE -> context.symphony.t.Title
    SongRepository.SortBy.ARTIST -> context.symphony.t.Artist
    SongRepository.SortBy.ALBUM -> context.symphony.t.Album
    SongRepository.SortBy.DURATION -> context.symphony.t.Duration
    SongRepository.SortBy.DATE_MODIFIED -> context.symphony.t.LastModified
    SongRepository.SortBy.COMPOSER -> context.symphony.t.Composer
    SongRepository.SortBy.ALBUM_ARTIST -> context.symphony.t.AlbumArtist
    SongRepository.SortBy.YEAR -> context.symphony.t.Year
    SongRepository.SortBy.FILENAME -> context.symphony.t.Filename
    SongRepository.SortBy.TRACK_NUMBER -> context.symphony.t.TrackNumber
}

fun SongListType.getLastUsedSortBy(context: ViewContext) = when (this) {
    SongListType.Default -> context.symphony.settings.lastUsedSongsSortBy
    SongListType.Album -> context.symphony.settings.lastUsedAlbumSongsSortBy
    SongListType.Playlist -> context.symphony.settings.lastUsedPlaylistSongsSortBy
}

fun SongListType.setLastUsedSortBy(context: ViewContext, sort: SongRepository.SortBy) =
    when (this) {
        SongListType.Default -> context.symphony.settings.setLastUsedSongsSortBy(sort)
        SongListType.Playlist -> context.symphony.settings.setLastUsedPlaylistSongsSortBy(sort)
        SongListType.Album -> context.symphony.settings.setLastUsedAlbumSongsSortBy(sort)
    }

fun SongListType.getLastUsedSortReverse(context: ViewContext) = when (this) {
    SongListType.Default -> context.symphony.settings.lastUsedSongsSortReverse
    SongListType.Playlist -> context.symphony.settings.lastUsedPlaylistSongsSortReverse
    SongListType.Album -> context.symphony.settings.lastUsedAlbumSongsSortReverse
}

fun SongListType.setLastUsedSortReverse(context: ViewContext, reverse: Boolean) = when (this) {
    SongListType.Default -> context.symphony.settings.setLastUsedSongsSortReverse(reverse)
    SongListType.Playlist -> context.symphony.settings.setLastUsedPlaylistSongsSortReverse(reverse)
    SongListType.Album -> context.symphony.settings.setLastUsedAlbumSongsSortReverse(reverse)
}
