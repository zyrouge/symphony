package io.github.zyrouge.symphony.ui.view.nowPlaying

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import coil.compose.AsyncImage
import io.github.zyrouge.symphony.ui.components.noRippleClickable
import io.github.zyrouge.symphony.ui.helpers.FadeTransition
import io.github.zyrouge.symphony.ui.helpers.RoutesBuilder
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.NowPlayingPlayerStateData
import io.github.zyrouge.symphony.ui.view.NowPlayingStates
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch


@Composable
fun NowPlayingBodyCover(
    context: ViewContext,
    data: NowPlayingPlayerStateData,
    states: NowPlayingStates,
) {
    val coroutineScope = rememberCoroutineScope()
    val showLyrics by states.showLyrics.collectAsState()
    val currentSong by rememberUpdatedState(data.song)
    var lyricsState by remember { mutableIntStateOf(0) }
    var lyricsSongId by remember { mutableStateOf<Long?>(null) }
    var lyrics by remember { mutableStateOf<String?>(null) }

    val fetchLyrics = { check: Boolean ->
        if (check && (lyricsSongId != currentSong.id || lyricsState == 0)) {
            lyricsState = 1
            coroutineScope.launch {
                lyricsSongId = currentSong.id
                lyrics = context.symphony.groove.lyrics.getLyrics(currentSong)
                lyricsState = 2
            }
        }
    }

    LaunchedEffect(LocalContext.current) {
        awaitAll(
            async { snapshotFlow { currentSong }.collect { fetchLyrics(showLyrics) } },
            async { snapshotFlow { showLyrics }.collect { fetchLyrics(it) } },
        )
    }

    data.run {
        BoxWithConstraints(modifier = Modifier.padding(defaultHorizontalPadding, 0.dp)) {
            val dimension = min(maxHeight, maxWidth)

            Box(
                modifier = Modifier
                    .size(dimension)
                    .aspectRatio(1f)
            ) {
                AnimatedContent(
                    label = "now-playing-body-cover",
                    modifier = Modifier.matchParentSize(),
                    targetState = showLyrics,
                    transitionSpec = {
                        FadeTransition.enterTransition()
                            .togetherWith(FadeTransition.exitTransition())
                    },
                ) { targetStateShowLyrics ->
                    when {
                        targetStateShowLyrics -> Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(12.dp),
                                )
                                .padding(16.dp, 12.dp)
                        ) {
                            ProvideTextStyle(
                                MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            ) {
                                when {
                                    lyricsState == 2 && lyrics != null -> AnimatedContent(
                                        label = "now-playing-body-cover-lyrics",
                                        targetState = lyrics ?: "",
                                        transitionSpec = {
                                            FadeTransition.enterTransition()
                                                .togetherWith(FadeTransition.exitTransition())
                                        },
                                    ) {
                                        Text(
                                            it,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .verticalScroll(rememberScrollState()),
                                        )
                                    }

                                    else -> Text(
                                        if (lyricsState == 1) context.symphony.t.Loading
                                        else context.symphony.t.NoLyrics,
                                        modifier = Modifier.align(Alignment.Center),
                                    )
                                }
                            }
                        }

                        else -> AnimatedContent(
                            label = "now-playing-body-cover-artwork",
                            modifier = Modifier.matchParentSize(),
                            targetState = song,
                            transitionSpec = {
                                FadeTransition.enterTransition()
                                    .togetherWith(FadeTransition.exitTransition())
                            },
                        ) { targetStateSong ->
                            AsyncImage(
                                targetStateSong
                                    .createArtworkImageRequest(context.symphony)
                                    .build(),
                                null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .noRippleClickable {
                                        context.navController.navigate(
                                            RoutesBuilder.buildAlbumRoute(song.albumId)
                                        )
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}
