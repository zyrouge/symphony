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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(UnstableApi::class)
class RadioPlayer(val symphony: Symphony) {
    data class PlayableMedia(val id: String, val uri: Uri) {
        fun toMediaItem() = MediaItem.Builder().setMediaId(id).setUri(uri).build()

        companion object {
            fun fromMediaItem(mediaItem: MediaItem): PlayableMedia {
                val uri = mediaItem.localConfiguration?.uri
                    ?: throw Exception("Missing media item uri")
                return PlayableMedia(mediaItem.mediaId, uri)
            }
        }
    }

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
    val mediaSession = MediaSession
        .Builder(symphony.applicationContext, interceptingMediaPlayer)
        .setCallback(mediaSessionCallback)
        .build()
    val mediaSessionId get() = mediaSession.id

//    val fadePlayback get() = symphony.settings.fadePlayback.value

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

    suspend fun hasNextMedia() = withMediaPlayer {
        it.currentMediaItemIndex + 1 < it.mediaItemCount
    }

    suspend fun getMedia() = withMediaPlayer {
        it.currentMediaItem?.let { mediaItem -> PlayableMedia.fromMediaItem(mediaItem) }
    }

    suspend fun getNextMedia() = withMediaPlayer {
        val nextIndex = it.currentMediaItemIndex + 1
        when {
            nextIndex < it.mediaItemCount -> PlayableMedia.fromMediaItem(it.getMediaItemAt(nextIndex))
            else -> null
        }
    }

    suspend fun setMedia(media: PlayableMedia) = withMediaPlayer {
        val mediaItem = media.toMediaItem()
        when (it.mediaItemCount) {
            0 -> it.setMediaItems(listOf(mediaItem))
            else -> it.replaceMediaItem(0, mediaItem)
        }
    }

    suspend fun setNextMedia(media: PlayableMedia?) = withMediaPlayer {
        if (media == null) {
            it.removeMediaItem(1)
            return@withMediaPlayer
        }
        val mediaItem = media.toMediaItem()
        when (it.mediaItemCount) {
            // this shouldn't happen
            0 -> it.setMediaItems(listOf(mediaItem, mediaItem))
            1 -> it.addMediaItem(mediaItem)
            else -> it.replaceMediaItem(1, mediaItem)
        }
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
        symphony.radio.onPlayerIsPlayingChanged(isPlaying)
    }

    fun onMediaPlayerVolumeChanged(volume: Float) {
    }

    fun onMediaPlayerPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
        symphony.radio.onPlayerPlaybackParametersChanged(
            speed = playbackParameters.speed,
            pitch = playbackParameters.pitch,
        )
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
        withMediaPlayerNoSuspend {
            if (it.mediaItemCount == 0) {
                return@withMediaPlayerNoSuspend
            }
            it.removeMediaItem(0)
        }
        symphony.radio.onPlayerSongEnded(Radio.SongEndedReason.Finish)
    }

    companion object {
        const val MIN_VOLUME = 0f
        const val MAX_VOLUME = 1f
        const val DUCK_VOLUME = 0.2f
        const val DEFAULT_SEEK = 0L
        const val DEFAULT_SPEED = 1f
        const val DEFAULT_PITCH = 1f
    }
}
