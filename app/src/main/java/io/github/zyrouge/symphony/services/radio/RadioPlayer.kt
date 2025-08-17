package io.github.zyrouge.symphony.services.radio

import android.net.Uri
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import io.github.zyrouge.symphony.Symphony
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer

@OptIn(UnstableApi::class)
class RadioPlayer(val symphony: Symphony) {
    data class PlaybackPosition(val played: Long, val total: Long) {
        val ratio: Float
            get() = (played.toFloat() / total).takeIf { it.isFinite() } ?: 0f

        companion object {
            val zero = PlaybackPosition(0L, 0L)
        }
    }

    private class ExoPlayerListener(val player: RadioPlayer) : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            player.onMediaPlayerIsPlayingChanged(isPlaying)
        }

        override fun onVolumeChanged(volume: Float) {
            player.onMediaPlayerVolumeChanged(volume)
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            player.onMediaPlayerPlaybackParametersChanged(playbackParameters)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            player.onMediaPlayerPlaybackStateChanged(playbackState)
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            player.onMediaPlayerMediaItemTransition(mediaItem, reason)
        }
    }

    private class InterceptedMediaPlayer(val player: RadioPlayer, mediaPlayer: ExoPlayer) :
        ForwardingPlayer(mediaPlayer)

    private class MediaSessionCallback(val player: RadioPlayer) : MediaSession.Callback

    private val mediaPlayerListener = ExoPlayerListener(this)
    private val mediaPlayerUnsafe = ExoPlayer.Builder(symphony.applicationContext)
        .setLooper(Looper.getMainLooper())
        .setAudioAttributes(AudioAttributes.DEFAULT, true)
        .setHandleAudioBecomingNoisy(true)
        .setWakeMode(C.WAKE_MODE_LOCAL)
        .build()

    private val interceptingMediaPlayer = InterceptedMediaPlayer(this, mediaPlayerUnsafe)
    private val mediaSessionCallback = MediaSessionCallback(this)
    private val mediaSession = MediaSession
        .Builder(symphony.applicationContext, interceptingMediaPlayer)
        .setCallback(mediaSessionCallback)
        .build()
    val mediaSessionId get() = mediaSession.id

//    val fadePlayback get() = symphony.settings.fadePlayback.value

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()
    private var playbackPositionUpdater: Timer? = null
    private val _playbackPosition = MutableStateFlow(PlaybackPosition.zero)
    val playbackPosition = _playbackPosition.asStateFlow()
    private val _volume = MutableStateFlow(MAX_VOLUME)
    val volume = _volume.asStateFlow()
    private val _speed = MutableStateFlow(DEFAULT_SPEED)
    val speed = _speed.asStateFlow()
    private val _pitch = MutableStateFlow(DEFAULT_PITCH)
    val pitch = _pitch.asStateFlow()

    init {
        symphony.groove.coroutineScope.launch {
            prepare()
        }
    }

    private suspend fun prepare() = withMediaPlayer {
        it.addListener(mediaPlayerListener)
        it.prepare()
    }

    suspend fun hasMedia() = withMediaPlayer { it.currentMediaItem != null }

    suspend fun hasNextMedia() = withMediaPlayer { it.getMediaItemAt(it.currentMediaItemIndex + 1) }

    suspend fun setMedia(uri: Uri) = withMediaPlayer {
        it.setMediaItems(listOf(MediaItem.fromUri(uri)))
    }

    suspend fun setNextMedia(uri: Uri) = withMediaPlayer {
        it.replaceMediaItem(1, MediaItem.fromUri(uri))
        it.play()
    }

    suspend fun play() = withMediaPlayer {
        it.play()
    }

    suspend fun pause() = withMediaPlayer {
        it.pause()
    }

    suspend fun stop() = withMediaPlayer {
        it.stop()
        it.clearMediaItems()
    }

    suspend fun seek(to: Long) = withMediaPlayer {
        it.seekTo(to)
    }

    suspend fun setVolume(to: Float) = withMediaPlayer {
        it.volume = to
    }

    suspend fun setSpeed(to: Float) = withMediaPlayer {
        it.playbackParameters = it.playbackParameters.withSpeed(to)
    }

    suspend fun setPitch(to: Float) = withMediaPlayer {
        it.playbackParameters = it.playbackParameters.withPitch(to)
    }

    private fun createDurationTimer() {
        playbackPositionUpdater = kotlin.concurrent.timer(period = 500L) {
            emitPlaybackPosition()
        }
    }

    private fun emitPlaybackPosition() {
        symphony.groove.coroutineScope.launch(Dispatchers.Main) {
            _playbackPosition.update {
                PlaybackPosition(mediaPlayerUnsafe.currentPosition, mediaPlayerUnsafe.duration)
            }
        }
    }

    private fun destroyDurationTimer() {
        playbackPositionUpdater?.cancel()
        playbackPositionUpdater = null
    }

    private suspend fun <T> withMediaPlayer(fn: (ExoPlayer) -> T): T {
        return withContext(Dispatchers.Main) {
            fn(mediaPlayerUnsafe)
        }
    }

    private fun withMediaPlayerNoSuspend(fn: (ExoPlayer) -> Unit) {
        symphony.groove.coroutineScope.launch(Dispatchers.Main) {
            fn(mediaPlayerUnsafe)
        }
    }

    fun onMediaPlayerIsPlayingChanged(isPlaying: Boolean) {
        when {
            isPlaying -> createDurationTimer()
            else -> destroyDurationTimer()
        }
        _isPlaying.update { isPlaying }
    }

    fun onMediaPlayerVolumeChanged(volume: Float) {
        _volume.update { volume }
    }

    fun onMediaPlayerPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
        _speed.update { playbackParameters.speed }
        _pitch.update { playbackParameters.pitch }
    }

    fun onMediaPlayerPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_ENDED) {
            onMediaPlayerMediaEnded()
        }
    }

    fun onMediaPlayerMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        onMediaPlayerMediaEnded()
    }

    private fun onMediaPlayerMediaEnded() {
        emitPlaybackPosition()
        withMediaPlayerNoSuspend {
            // keep playing item at 0
            if (it.mediaItemCount == 0) {
                return@withMediaPlayerNoSuspend
            }
            it.removeMediaItem(0)
        }
    }

    companion object {
        const val MIN_VOLUME = 0f
        const val MAX_VOLUME = 1f
        const val DUCK_VOLUME = 0.2f
        const val DEFAULT_SPEED = 1f
        const val DEFAULT_PITCH = 1f
    }
}
