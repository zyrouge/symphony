package io.github.zyrouge.symphony.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.zyrouge.symphony.services.groove.entities.Song
import io.github.zyrouge.symphony.services.radio.RadioQueue
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.nowPlaying.NowPlayingNothingPlaying
import io.github.zyrouge.symphony.ui.view.nowPlaying.NowPlayingBody
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable

@Immutable
data class NowPlayingData(
    val song: Song,
    val isPlaying: Boolean,
    val currentSongIndex: Int,
    val queueSize: Int,
    val currentLoopMode: RadioQueue.LoopMode,
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
    CompactLeft,
    CompactRight,
    Traditional,
}

enum class NowPlayingLyricsLayout {
    ReplaceArtwork,
    SeparatePage,
}

@Serializable
object NowPlayingViewRoute

@Composable
fun NowPlayingView(context: ViewContext) {
    NowPlayingObserver(context) { data ->
        when {
            data != null -> NowPlayingBody(context, data = data)
            else -> NowPlayingNothingPlaying(context)
        }
    }
}

@Composable
fun NowPlayingObserver(
    context: ViewContext,
    content: @Composable (NowPlayingData?) -> Unit,
) {
    val queue by context.symphony.radio.observatory.queue.collectAsStateWithLifecycle()
    val song by remember {
        derivedStateOf {
            queue.firstOrNull { it. }
        }
    }
    val isViable by remember(song) {
        derivedStateOf { song != null }
    }

    val isPlaying by context.symphony.radio.observatory.isPlaying.collectAsStateWithLifecycle()
    val currentLoopMode by context.symphony.radio.observatory.loopMode.collectAsStateWithLifecycle()
    val currentShuffleMode by context.symphony.radio.observatory.shuffleMode.collectAsStateWithLifecycle()
    val currentSpeed by context.symphony.radio.observatory.speed.collectAsStateWithLifecycle()
    val currentPitch by context.symphony.radio.observatory.pitch.collectAsStateWithLifecycle()
    val persistedSpeed by context.symphony.radio.observatory.persistedSpeed.collectAsStateWithLifecycle()
    val persistedPitch by context.symphony.radio.observatory.persistedPitch.collectAsStateWithLifecycle()
    val sleepTimer by context.symphony.radio.observatory.sleepTimer.collectAsStateWithLifecycle()
    val pauseOnCurrentSongEnd by context.symphony.radio.observatory.pauseOnCurrentSongEnd.collectAsStateWithLifecycle()
    val showSongAdditionalInfo by context.symphony.settings.nowPlayingAdditionalInfo.flow.collectAsStateWithLifecycle()
    val enableSeekControls by context.symphony.settings.nowPlayingSeekControls.flow.collectAsStateWithLifecycle()
    val seekBackDuration by context.symphony.settings.seekBackDuration.flow.collectAsStateWithLifecycle()
    val seekForwardDuration by context.symphony.settings.seekForwardDuration.flow.collectAsStateWithLifecycle()
    val controlsLayout by context.symphony.settings.nowPlayingControlsLayout.flow.collectAsStateWithLifecycle()
    val lyricsLayout by context.symphony.settings.nowPlayingLyricsLayout.flow.collectAsStateWithLifecycle()

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
