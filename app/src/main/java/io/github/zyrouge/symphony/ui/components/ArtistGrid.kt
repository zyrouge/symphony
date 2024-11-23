package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import io.github.zyrouge.symphony.services.groove.repositories.ArtistRepository
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistGrid(
    context: ViewContext,
    artistName: List<String>,
    artistsCount: Int? = null,
) {
    val sortBy by context.symphony.settings.lastUsedArtistsSortBy.flow.collectAsState()
    val sortReverse by context.symphony.settings.lastUsedArtistsSortReverse.flow.collectAsState()
    val sortedArtistNames by remember(artistName, sortBy, sortReverse) {
        derivedStateOf {
            context.symphony.groove.artist.sort(artistName, sortBy, sortReverse)
        }
    }
    val tileSize by context.symphony.settings.lastUsedArtistsTileSize.flow.collectAsState()

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    MediaSortBarScaffold(
        mediaSortBar = {
            MediaSortBar(
                context,
                reverse = sortReverse,
                onReverseChange = {
                    context.symphony.settings.lastUsedArtistsSortReverse.setValue(it)
                },
                sort = sortBy,
                sorts = ArtistRepository.SortBy.entries
                    .associateWith { x -> ViewContext.parameterizedFn { x.label(it) } },
                onSortChange = {
                    context.symphony.settings.lastUsedArtistsSortBy.setValue(it)
                },
                label = {
                    Text(context.symphony.t.XArtists((artistsCount ?: artistName.size).toString()))
                },
                onShowSheet = { showBottomSheet = true },
            )
        },
        content = {
            when {
                artistName.isEmpty() -> IconTextBody(
                    icon = { modifier ->
                        Icon(
                            Icons.Filled.Person,
                            null,
                            modifier = modifier,
                        )
                    },
                    content = { Text(context.symphony.t.DamnThisIsSoEmpty) }
                )

                else -> ResponsiveGrid(tileSize) {
                    itemsIndexed(
                        sortedArtistNames,
                        key = { i, x -> "$i-$x" },
                        contentType = { _, _ -> Groove.Kind.ARTIST }
                    ) { _, artistName ->
                        context.symphony.groove.artist.get(artistName)?.let { artist ->
                            ArtistTile(context, artist)
                        }
                    }
                }
            }

            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false },
                    sheetState = sheetState
                ) {
                    ResponsiveGridSizeAdjust(
                        context,
                        tileSize,
                        onTileSizeChange = {
                            context.symphony.settings.lastUsedArtistsTileSize.setValue(
                                it
                            )
                        },
                    )
                }
            }
        }
    )
}

private fun ArtistRepository.SortBy.label(context: ViewContext) = when (this) {
    ArtistRepository.SortBy.CUSTOM -> context.symphony.t.Custom
    ArtistRepository.SortBy.ARTIST_NAME -> context.symphony.t.Artist
    ArtistRepository.SortBy.ALBUMS_COUNT -> context.symphony.t.AlbumCount
    ArtistRepository.SortBy.TRACKS_COUNT -> context.symphony.t.TrackCount
}
