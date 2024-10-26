package io.github.zyrouge.symphony.ui.view

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.services.radio.RadioLoopMode
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.nowPlaying.NothingPlaying
import io.github.zyrouge.symphony.ui.view.nowPlaying.NowPlayingBody
import kotlinx.coroutines.flow.MutableStateFlow

@Immutable
data class NowPlayingData(
    val song: Song,
    val isPlaying: Boolean,
    val currentSongIndex: Int,
    val queueSize: Int,
    val currentLoopMode: RadioLoopMode,
    val currentShuffleMode: Boolean,
    val currentSpeed: Float,
    val currentPitch: Float,
    val persistedSpeed: Float,
    val persistedPitch: Float,
    val hasSleepTimer: Boolean,
    val pauseOnCurrentSongEnd: Boolean,
    val showSongAdditionalInfo: Boolean,
    val enableSeekControls: Boolean,
    val seekBackDuration: Int,
    val seekForwardDuration: Int,
    val controlsLayout: NowPlayingControlsLayout,
    val lyricsLayout: NowPlayingLyricsLayout,
)

data class NowPlayingStates(
    val showLyrics: MutableStateFlow<Boolean>,
)

object NowPlayingDefaults {
    var showLyrics = false
}

enum class NowPlayingControlsLayout {
    Default,
    Traditional,
}

enum class NowPlayingLyricsLayout {
    ReplaceArtwork,
    SeparatePage,
}

@Composable
fun NowPlayingView(context: ViewContext) {
    BackHandler {
        context.navController.popBackStack()
    }

    NowPlayingWithData(context) { data ->
        when {
            data != null -> NowPlayingBody(context, data = data)
            else -> NothingPlaying(context)
        }
    }
}

@Composable
fun NowPlayingWithData(
    context: ViewContext,
    content: @Composable (NowPlayingData?) -> Unit,
) {
    val queue by context.symphony.radio.observatory.queue.collectAsState()
    val queueIndex by context.symphony.radio.observatory.queueIndex.collectAsState()
    val song by remember(queue, queueIndex) {
        derivedStateOf {
            queue.getOrNull(queueIndex)?.let { context.symphony.groove.song.get(it) }
        }
    }
    val isPlaying by context.symphony.radio.observatory.isPlaying.collectAsState()
    val currentLoopMode by context.symphony.radio.observatory.loopMode.collectAsState()
    val currentShuffleMode by context.symphony.radio.observatory.shuffleMode.collectAsState()
    val currentSpeed by context.symphony.radio.observatory.speed.collectAsState()
    val currentPitch by context.symphony.radio.observatory.pitch.collectAsState()
    val persistedSpeed by context.symphony.radio.observatory.persistedSpeed.collectAsState()
    val persistedPitch by context.symphony.radio.observatory.persistedPitch.collectAsState()
    val sleepTimer by context.symphony.radio.observatory.sleepTimer.collectAsState()
    val pauseOnCurrentSongEnd by context.symphony.radio.observatory.pauseOnCurrentSongEnd.collectAsState()
    val showSongAdditionalInfo by context.symphony.settings.nowPlayingAdditionalInfo.collectAsState()
    val enableSeekControls by context.symphony.settings.nowPlayingSeekControls.collectAsState()
    val seekBackDuration by context.symphony.settings.seekBackDuration.collectAsState()
    val seekForwardDuration by context.symphony.settings.seekForwardDuration.collectAsState()
    val controlsLayout by context.symphony.settings.nowPlayingControlsLayout.collectAsState()
    val lyricsLayout by context.symphony.settings.nowPlayingLyricsLayout.collectAsState()
    val isViable by remember(song) {
        derivedStateOf { song != null }
    }

    val data = when {
        isViable -> NowPlayingData(
            song = song!!,
            isPlaying = isPlaying,
            currentSongIndex = queueIndex,
            queueSize = queue.size,
            currentLoopMode = currentLoopMode,
            currentShuffleMode = currentShuffleMode,
            currentSpeed = currentSpeed,
            currentPitch = currentPitch,
            persistedSpeed = persistedSpeed,
            persistedPitch = persistedPitch,
            hasSleepTimer = sleepTimer != null,
            pauseOnCurrentSongEnd = pauseOnCurrentSongEnd,
            showSongAdditionalInfo = showSongAdditionalInfo,
            enableSeekControls = enableSeekControls,
            seekBackDuration = seekBackDuration,
            seekForwardDuration = seekForwardDuration,
            controlsLayout = controlsLayout,
            lyricsLayout = lyricsLayout,
        )

        else -> null
    }
    content(data)
}
