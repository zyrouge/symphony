package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.Genre
import io.github.zyrouge.symphony.services.groove.GenreRepository
import io.github.zyrouge.symphony.services.groove.GenreSortBy
import io.github.zyrouge.symphony.services.groove.GrooveKinds
import io.github.zyrouge.symphony.ui.helpers.RoutesBuilder
import io.github.zyrouge.symphony.ui.helpers.ViewContext

private object GenreTile {
    val colors = mutableListOf(
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
    genres: List<Genre>,
    isLoading: Boolean = false,
) {
    var sortBy by remember {
        mutableStateOf(
            context.symphony.settings.getLastUsedGenresSortBy() ?: GenreSortBy.GENRE
        )
    }
    var sortReverse by remember {
        mutableStateOf(context.symphony.settings.getLastUsedGenresSortReverse())
    }
    val sortedGenres by remember {
        derivedStateOf { GenreRepository.sort(genres, sortBy, sortReverse) }
    }

    ResponsiveGrid(
        topBar = {
            Box(modifier = Modifier.padding(bottom = 4.dp)) {
                MediaSortBar(
                    context,
                    reverse = sortReverse,
                    onReverseChange = {
                        sortReverse = it
                        context.symphony.settings.setLastUsedGenresSortReverse(it)
                    },
                    sort = sortBy,
                    sorts = GenreSortBy.values().associateWith { x -> { x.label(it) } },
                    onSortChange = {
                        sortBy = it
                        context.symphony.settings.setLastUsedGenresSortBy(it)
                    },
                    label = {
                        Text(context.symphony.t.XGenres(genres.size))
                    },
                    isLoading = isLoading,
                )
            }
        },
        content = { gridData ->
            itemsIndexed(
                sortedGenres,
                key = { _, x -> x.name },
                contentType = { _, _ -> GrooveKinds.GENRE }
            ) { i, genre ->
                Card(
                    modifier = Modifier.padding(
                        start = if (i % gridData.columnsCount == 0) 12.dp else 0.dp,
                        end = if ((i - 1) % gridData.columnsCount == 0) 12.dp else 8.dp,
                        bottom = 8.dp,
                    ),
                    colors = GenreTile.cardColors(i),
                    onClick = {
                        context.navController.navigate(RoutesBuilder.buildGenreRoute(genre.name))
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
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
                                genre.name,
                                textAlign = TextAlign.Start,
                                style = MaterialTheme.typography.displaySmall
                                    .copy(fontWeight = FontWeight.Bold),
                                softWrap = false,
                                overflow = TextOverflow.Clip,
                            )
                        }
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                genre.name,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge
                                    .copy(fontWeight = FontWeight.Bold),
                            )
                            Text(
                                context.symphony.t.XSongs(genre.numberOfTracks),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }
        }
    )
}

private fun GenreSortBy.label(context: ViewContext) = when (this) {
    GenreSortBy.CUSTOM -> context.symphony.t.custom
    GenreSortBy.GENRE -> context.symphony.t.genre
    GenreSortBy.TRACKS_COUNT -> context.symphony.t.trackCount
}
