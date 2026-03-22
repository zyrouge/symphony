package io.github.zyrouge.symphony.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.zyrouge.symphony.services.radio.RadioPlayer
import io.github.zyrouge.symphony.ui.helpers.FadeTransition
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.TimedContent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun LyricsText(
    context: ViewContext,
    padding: PaddingValues,
    style: TimedContentTextStyle,
) {
    val coroutineScope = rememberCoroutineScope()
    val songFlow = remember {
        context.symphony.radio.getQueueAsFlow()
            .mapLatest { it?.entity?.playingId }
            .flatMapLatest { playingId ->
                playingId?.let { context.symphony.groove.song.findByIdAsFlow(it) } ?: emptyFlow()
            }
    }
    val song by songFlow.collectAsStateWithLifecycle(null)
    val playbackPosition by remember { context.symphony.radio.getPlaybackPositionAsFlow() }
        .collectAsStateWithLifecycle(RadioPlayer.PlaybackPosition.zero)
    val lyrics by songFlow
        .flatMapLatest { song ->
            song?.id?.let { context.symphony.groove.song.findLyricsOfIdAsFlow(it) }
                ?: emptyFlow()
        }
        .mapLatest { it?.let { TimedContent.fromLyrics(it.lyrics) } }
        .collectAsStateWithLifecycle(null)

    AnimatedContent(
        label = "lyrics-text",
        targetState = lyrics,
        transitionSpec = {
            FadeTransition.enterTransition()
                .togetherWith(FadeTransition.exitTransition())
        },
    ) { targetLyrics ->
        when {
            targetLyrics != null -> TimedContentText(
                content = targetLyrics,
                duration = playbackPosition.played,
                padding = padding,
                style = style,
                onSeek = {
                    targetLyrics.pairs.getOrNull(it)?.first?.let { to ->
                        coroutineScope.launch {
                            context.symphony.radio.seek(to)
                        }
                    }
                }
            )

            else -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(context.symphony.t.NoLyrics)
            }
        }
    }
}
