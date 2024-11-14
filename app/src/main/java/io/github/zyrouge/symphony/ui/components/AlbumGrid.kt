package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.zyrouge.symphony.services.groove.Groove
import io.github.zyrouge.symphony.services.groove.repositories.AlbumRepository
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumGrid(
    context: ViewContext,
    albumIds: List<String>,
    albumsCount: Int? = null,
) {
    val sortBy by context.symphony.settings.lastUsedAlbumsSortBy.flow.collectAsState()
    val sortReverse by context.symphony.settings.lastUsedAlbumsSortReverse.flow.collectAsState()
    val sortedAlbumIds by remember(albumIds, sortBy, sortReverse) {
        derivedStateOf {
            context.symphony.groove.album.sort(albumIds, sortBy, sortReverse)
        }
    }
    val tileSize by context.symphony.settings.lastUsedAlbumsTileSize.flow.collectAsState()

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    MediaSortBarScaffold(
        mediaSortBar = {
            MediaSortBar(
                context,
                reverse = sortReverse,
                onReverseChange = {
                    context.symphony.settings.lastUsedAlbumsSortReverse.setValue(it)
                },
                sort = sortBy,
                sorts = AlbumRepository.SortBy.entries.associateWith { x ->
                    ViewContext.parameterizedFn { x.label(it) }
                },
                onSortChange = {
                    context.symphony.settings.lastUsedAlbumsSortBy.setValue(it)
                },
                label = {
                    Text(context.symphony.t.XAlbums((albumsCount ?: albumIds.size).toString()))
                },
            )
        },
        content = {
            when {
                albumIds.isEmpty() -> IconTextBody(
                    icon = { modifier ->
                        Icon(
                            Icons.Filled.Album,
                            null,
                            modifier = modifier,
                        )
                    },
                    content = { Text(context.symphony.t.DamnThisIsSoEmpty) }
                )

                else -> ResponsiveGrid(tileSize) {
                    itemsIndexed(
                        sortedAlbumIds,
                        key = { i, x -> "$i-$x" },
                        contentType = { _, _ -> Groove.Kinds.ALBUM }
                    ) { _, albumId ->
                        context.symphony.groove.album.get(albumId)?.let { album ->
                            AlbumTile(context, album)
                        }
                    }
                }
            }

            Button(onClick = { showBottomSheet = true }) { Text("bottom sheet") }
            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false },
                    sheetState = sheetState
                ) {
                    ResponsiveGridSizeAdjust(
                        tileSize,
                        onTileSizeChange = {
                            context.symphony.settings.lastUsedAlbumsTileSize.setValue(
                                it
                            )
                        },
                    )
                }
            }
        }
    )
}

private fun AlbumRepository.SortBy.label(context: ViewContext) = when (this) {
    AlbumRepository.SortBy.CUSTOM -> context.symphony.t.Custom
    AlbumRepository.SortBy.ALBUM_NAME -> context.symphony.t.Album
    AlbumRepository.SortBy.ARTIST_NAME -> context.symphony.t.Artist
    AlbumRepository.SortBy.TRACKS_COUNT -> context.symphony.t.TrackCount
}
