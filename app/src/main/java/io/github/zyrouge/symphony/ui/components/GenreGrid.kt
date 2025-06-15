package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.Groove
import io.github.zyrouge.symphony.services.groove.entities.Genre
import io.github.zyrouge.symphony.services.groove.repositories.GenreRepository
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.GenreViewRoute

private object GenreTile {
    val colors = listOf(
        0xFFEF4444,
        0xFFF97316,
        0xFFF59E0B,
        0xFF16A34A,
        0xFF06B6B4,
        0xFF8B5CF6,
        0xFFD946EF,
        0xFFF43F5E,
        0xFF6366F1,
        0xFFA855F7,
    ).map { Color(it) }

    fun colorAt(index: Int) = colors[index % colors.size]

    @Composable
    fun cardColors(index: Int) = CardDefaults.cardColors(
        containerColor = colorAt(index),
        contentColor = Color.White,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreGrid(
    context: ViewContext,
    attributedGenres: List<Genre.AlongAttributes>,
    sortBy: GenreRepository.SortBy,
    sortReverse: Boolean,
) {
    val horizontalGridColumns by context.symphony.settings.lastUsedGenresHorizontalGridColumns.flow.collectAsStateWithLifecycle()
    val verticalGridColumns by context.symphony.settings.lastUsedGenresVerticalGridColumns.flow.collectAsStateWithLifecycle()
    val gridColumns by remember(horizontalGridColumns, verticalGridColumns) {
        derivedStateOf {
            ResponsiveGridColumns(horizontalGridColumns, verticalGridColumns)
        }
    }
    var showModifyLayoutSheet by remember { mutableStateOf(false) }

    MediaSortBarScaffold(
        mediaSortBar = {
            Box(modifier = Modifier.padding(bottom = 4.dp)) {
                MediaSortBar(
                    context,
                    reverse = sortReverse,
                    onReverseChange = {
                        context.symphony.settings.lastUsedGenresSortReverse.setValue(it)
                    },
                    sort = sortBy,
                    sorts = GenreRepository.SortBy.entries
                        .associateWith { x -> ViewContext.parameterizedFn { x.label(it) } },
                    onSortChange = {
                        context.symphony.settings.lastUsedGenresSortBy.setValue(it)
                    },
                    label = {
                        Text(context.symphony.t.XGenres(attributedGenres.size.toString()))
                    },
                    onShowModifyLayout = {
                        showModifyLayoutSheet = true
                    },
                )
            }
        },
        content = {
            when {
                attributedGenres.isEmpty() -> IconTextBody(
                    icon = { modifier ->
                        Icon(
                            Icons.Filled.MusicNote,
                            null,
                            modifier = modifier,
                        )
                    },
                    content = { Text(context.symphony.t.DamnThisIsSoEmpty) }
                )

                else -> ResponsiveGrid(gridColumns) { gridData ->
                    itemsIndexed(
                        attributedGenres,
                        key = { i, x -> "$i-$x" },
                        contentType = { _, _ -> Groove.Kind.GENRE }
                    ) { i, attributedGenre ->
                        GenreTile(
                            context,
                            index = i,
                            columnsCount = gridData.columnsCount,
                            attributedGenre = attributedGenre,
                        )
                    }
                }
            }

            if (showModifyLayoutSheet) {
                ResponsiveGridSizeAdjustBottomSheet(
                    context,
                    columns = gridColumns,
                    onColumnsChange = {
                        context.symphony.settings.lastUsedGenresHorizontalGridColumns.setValue(
                            it.horizontal
                        )
                        context.symphony.settings.lastUsedGenresVerticalGridColumns.setValue(
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

@Composable
private fun GenreTile(
    context: ViewContext,
    index: Int,
    columnsCount: Int,
    attributedGenre: Genre.AlongAttributes,
) {
    Card(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .padding(
                start = if (index % columnsCount == 0) 12.dp else 0.dp,
                end = if ((index - 1) % columnsCount == 0) 12.dp else 8.dp,
                bottom = 8.dp,
            ),
        colors = GenreTile.cardColors(index),
        onClick = {
            context.navController.navigate(GenreViewRoute(attributedGenre.entity.name))
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .defaultMinSize(minHeight = 88.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .matchParentSize()
                    .fillMaxWidth()
                    .alpha(0.25f)
                    .absoluteOffset(8.dp, 12.dp)
            ) {
                Text(
                    attributedGenre.entity.name,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.displaySmall
                        .copy(fontWeight = FontWeight.Bold),
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                )
            }
            Column(
                modifier = Modifier.padding(8.dp, 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    attributedGenre.entity.name,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                        .copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    context.symphony.t.XSongs(attributedGenre.tracksCount.toString()),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

private fun GenreRepository.SortBy.label(context: ViewContext) = when (this) {
    GenreRepository.SortBy.CUSTOM -> context.symphony.t.Custom
    GenreRepository.SortBy.GENRE -> context.symphony.t.Genre
    GenreRepository.SortBy.TRACKS_COUNT -> context.symphony.t.TrackCount
}
