package io.github.zyrouge.symphony.ui.view.nowPlaying

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import coil.compose.AsyncImage
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.components.LyricsText
import io.github.zyrouge.symphony.ui.components.TimedContentTextStyle
import io.github.zyrouge.symphony.ui.components.swipeable
import io.github.zyrouge.symphony.ui.helpers.FadeTransition
import io.github.zyrouge.symphony.ui.helpers.Routes
import io.github.zyrouge.symphony.ui.helpers.ScreenOrientation
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.NowPlayingData
import io.github.zyrouge.symphony.ui.view.NowPlayingStates

@Composable
fun NowPlayingBodyCover(
    context: ViewContext,
    data: NowPlayingData,
    states: NowPlayingStates,
    orientation: ScreenOrientation,
) {
    val showLyrics by states.showLyrics.collectAsState()

    Box(modifier = Modifier.padding(defaultHorizontalPadding, 0.dp)) {
        AnimatedContent(
            label = "now-playing-body-cover",
            targetState = showLyrics,
            contentAlignment = Alignment.Center,
            transitionSpec = {
                val from = FadeTransition.enterTransition()
                val to = FadeTransition.exitTransition()
                from togetherWith to
            },
        ) { targetStateShowLyrics ->
            if (targetStateShowLyrics) {
                NowPlayingBodyCoverLyrics(context, orientation)
            } else {
                NowPlayingBodyCoverArtwork(context, data.song)
            }
        }
    }
}

@Composable
private fun NowPlayingBodyCoverLyrics(context: ViewContext, orientation: ScreenOrientation) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                0.dp,
                if (orientation == ScreenOrientation.LANDSCAPE) 0.dp else 8.dp
            )
            .background(
                MaterialTheme.colorScheme.surfaceContainer,
                RoundedCornerShape(12.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        LyricsText(
            context,
            padding = PaddingValues(
                horizontal = 12.dp,
                vertical = 8.dp,
            ),
            style = TimedContentTextStyle.defaultStyle(
                textStyle = LocalTextStyle.current,
                contentColor = LocalContentColor.current,
            ),
        )
    }
}

@Composable
private fun NowPlayingBodyCoverArtwork(context: ViewContext, song: Song) {
    BoxWithConstraints {
        val dimension = min(maxHeight, maxWidth)

        AnimatedContent(
            label = "now-playing-body-cover-artwork",
            modifier = Modifier.size(dimension),
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
                filterQuality = FilterQuality.High,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .swipeable(
                        minimumDragAmount = 100f,
                        onSwipeLeft = {
                            if (context.symphony.radio.canJumpToNext()) {
                                context.symphony.radio.jumpToNext()
                            }
                        },
                        onSwipeRight = {
                            if (context.symphony.radio.canJumpToPrevious()) {
                                context.symphony.radio.jumpToPrevious()
                            }
                        },
                    )
                    .pointerInput(Unit) {
                        detectTapGestures { _ ->
                            context.symphony.groove.album
                                .getIdFromSong(song)
                                ?.let {
                                    context.navController.navigate(
                                        Routes.Album.build(it)
                                    )
                                }
                        }
                    }
            )
        }

    }
}
