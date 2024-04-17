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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.GenreSortBy
import io.github.zyrouge.symphony.services.groove.GrooveKinds
import io.github.zyrouge.symphony.ui.helpers.Routes
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.wrapInViewContext

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
    genreNames: List<String>,
    genresCount: Int? = null,
) {
    val sortBy by context.symphony.settings.lastUsedGenresSortBy.collectAsState()
    val sortReverse by context.symphony.settings.lastUsedGenresSortReverse.collectAsState()
    val sortedGenreNames by remember(genreNames, sortBy, sortReverse) {
        derivedStateOf {
            context.symphony.groove.genre.sort(genreNames, sortBy, sortReverse)
        }
    }

    MediaSortBarScaffold(
        mediaSortBar = {
            Box(modifier = Modifier.padding(bottom = 4.dp)) {
                MediaSortBar(
                    context,
                    reverse = sortReverse,
                    onReverseChange = {
                        context.symphony.settings.setLastUsedGenresSortReverse(it)
                    },
                    sort = sortBy,
                    sorts = GenreSortBy.entries
                        .associateWith { x -> wrapInViewContext { x.label(it) } },
                    onSortChange = {
                        context.symphony.settings.setLastUsedGenresSortBy(it)
                    },
                    label = {
                        Text(
                            context.symphony.t.XGenres(
                                (genresCount ?: genreNames.size).toString()
                            )
                        )
                    },
                )
            }
        },
        content = {
            when {
                genreNames.isEmpty() -> IconTextBody(
                    icon = { modifier ->
                        Icon(
                            Icons.Filled.MusicNote,
                            null,
                            modifier = modifier,
                        )
                    },
                    content = { Text(context.symphony.t.DamnThisIsSoEmpty) }
                )

                else -> ResponsiveGrid { gridData ->
                    itemsIndexed(
                        sortedGenreNames,
                        key = { i, x -> "$i-$x" },
                        contentType = { _, _ -> GrooveKinds.GENRE }
                    ) { i, genreName ->
                        context.symphony.groove.genre.get(genreName)?.let { genre ->
                            Card(
                                modifier = Modifier
                                    .height(IntrinsicSize.Min)
                                    .padding(
                                        start = if (i % gridData.columnsCount == 0) 12.dp else 0.dp,
                                        end = if ((i - 1) % gridData.columnsCount == 0) 12.dp else 8.dp,
                                        bottom = 8.dp,
                                    ),
                                colors = GenreTile.cardColors(i),
                                onClick = {
                                    context.navController.navigate(Routes.Genre.build(genre.name))
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
                                            context.symphony.t.XSongs(genre.numberOfTracks.toString()),
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

private fun GenreSortBy.label(context: ViewContext) = when (this) {
    GenreSortBy.CUSTOM -> context.symphony.t.Custom
    GenreSortBy.GENRE -> context.symphony.t.Genre
    GenreSortBy.TRACKS_COUNT -> context.symphony.t.TrackCount
}
