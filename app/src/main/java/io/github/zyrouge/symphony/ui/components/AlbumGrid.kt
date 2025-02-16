package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.zyrouge.symphony.services.groove.Groove
import io.github.zyrouge.symphony.services.groove.entities.Album
import io.github.zyrouge.symphony.services.groove.repositories.AlbumRepository
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumGrid(
    context: ViewContext,
    albums: List<Album>,
    sortBy: AlbumRepository.SortBy,
    sortReverse: Boolean,
) {
    val horizontalGridColumns by context.symphony.settings.lastUsedAlbumsHorizontalGridColumns.flow.collectAsStateWithLifecycle()
    val verticalGridColumns by context.symphony.settings.lastUsedAlbumsVerticalGridColumns.flow.collectAsStateWithLifecycle()
    val gridColumns by remember(horizontalGridColumns, verticalGridColumns) {
        derivedStateOf {
            ResponsiveGridColumns(horizontalGridColumns, verticalGridColumns)
        }
    }
    var showModifyLayoutSheet by remember { mutableStateOf(false) }

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
                    Text(context.symphony.t.XAlbums(albums.size.toString()))
                },
                onShowModifyLayout = {
                    showModifyLayoutSheet = true
                },
            )
        },
        content = {
            when {
                albums.isEmpty() -> IconTextBody(
                    icon = { modifier ->
                        Icon(
                            Icons.Filled.Album,
                            null,
                            modifier = modifier,
                        )
                    },
                    content = { Text(context.symphony.t.DamnThisIsSoEmpty) }
                )

                else -> ResponsiveGrid(gridColumns) {
                    itemsIndexed(
                        albums,
                        key = { i, x -> "$i-$x" },
                        contentType = { _, _ -> Groove.Kind.ALBUM }
                    ) { _, album ->
                        AlbumTile(context, album)
                    }
                }
            }

            if (showModifyLayoutSheet) {
                ResponsiveGridSizeAdjustBottomSheet(
                    context,
                    columns = gridColumns,
                    onColumnsChange = {
                        context.symphony.settings.lastUsedAlbumsHorizontalGridColumns.setValue(
                            it.horizontal
                        )
                        context.symphony.settings.lastUsedAlbumsVerticalGridColumns.setValue(
                            it.vertical
                        )
                    },
                    onDismissRequest = {
                        showModifyLayoutSheet = false
                    }
                )
            }
        }
    )
}

private fun AlbumRepository.SortBy.label(context: ViewContext) = when (this) {
    AlbumRepository.SortBy.CUSTOM -> context.symphony.t.Custom
    AlbumRepository.SortBy.ALBUM_NAME -> context.symphony.t.Album
    AlbumRepository.SortBy.ARTIST_NAME -> context.symphony.t.Artist
    AlbumRepository.SortBy.TRACKS_COUNT -> context.symphony.t.TrackCount
    AlbumRepository.SortBy.ARTISTS_COUNT -> context.symphony.t.ArtistCount
    AlbumRepository.SortBy.YEAR -> context.symphony.t.Year
}
