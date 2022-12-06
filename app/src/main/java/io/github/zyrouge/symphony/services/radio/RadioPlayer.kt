package io.github.zyrouge.symphony.services.radio

import android.media.MediaPlayer
import android.net.Uri
import io.github.zyrouge.symphony.Symphony
import java.util.*

data class PlaybackPosition(
    val played: Int,
    val total: Int,
) {
    val ratio: Float
        get() = (played.toFloat() / total).takeIf { it.isFinite() } ?: 0f

    companion object {
        val zero = PlaybackPosition(0, 0)
    }
}

typealias RadioPlayerOnPreparedListener = () -> Unit
typealias RadioPlayerOnPlaybackPositionUpdateListener = (PlaybackPosition) -> Unit
typealias RadioPlayerOnFinishListener = () -> Unit

class RadioPlayer(val symphony: Symphony, uri: Uri) {
    var usable = false
    private val unsafeMediaPlayer: MediaPlayer
    private val mediaPlayer: MediaPlayer?
        get() = if (usable) unsafeMediaPlayer else null

    private var onPrepared: RadioPlayerOnPreparedListener? = null
    private var onPlaybackPositionUpdate: RadioPlayerOnPlaybackPositionUpdateListener? = null
    private var onFinish: RadioPlayerOnFinishListener? = null
    private var playbackPositionUpdater: Timer? = null
    val playbackPosition: PlaybackPosition?
        get() = mediaPlayer?.let {
            try {
                PlaybackPosition(
                    played = it.currentPosition,
                    total = it.duration
                )
            } catch (_: IllegalStateException) {
                null
            }
        }

    var volume: Float = MAX_VOLUME
    val fadePlayback: Boolean
        get() = symphony.settings.getFadePlayback()
    private val fadePlaybackDuration: Int
        get() = (symphony.settings.getFadePlaybackDuration() * 1000).toInt()
    private var fader: RadioEffects.Fader? = null
    val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying ?: false

    init {
        unsafeMediaPlayer = MediaPlayer().apply {
            setOnPreparedListener {
                usable = true
                createDurationTimer()
                onPrepared?.invoke()
            }
            setOnCompletionListener {
                usable = false
                onFinish?.invoke()
            }
            setOnErrorListener { _, _, _ ->
                true
            }
            setDataSource(symphony.applicationContext, uri)
        }
    }

    fun prepare(listener: RadioPlayerOnPreparedListener) {
        onPrepared = listener
        unsafeMediaPlayer.prepareAsync()
    }

    fun stop() {
        usable = false
        destroyDurationTimer()
        unsafeMediaPlayer.stop()
        unsafeMediaPlayer.reset()
        unsafeMediaPlayer.release()
    }

    fun start() = mediaPlayer?.start()
    fun pause() = mediaPlayer?.pause()
    fun seek(to: Int) = mediaPlayer?.seekTo(to)

    @JvmName("setVolumeTo")
    fun setVolume(to: Float, onFinish: (Boolean) -> Unit) {
        fader?.stop()
        when {
            to == volume -> onFinish(true)
            fadePlayback -> {
                fader = RadioEffects.Fader(
                    RadioEffects.Fader.Options(volume, to, fadePlaybackDuration),
                    onUpdate = {
                        setVolumeInstant(it)
                    },
                    onFinish = {
                        onFinish(it)
                        fader = null
                    }
                )
                fader?.start()
            }
            else -> {
                setVolumeInstant(to)
                onFinish(true)
            }
        }
    }

    fun setVolumeInstant(to: Float) {
        volume = to
        mediaPlayer?.setVolume(to, to)
    }

    fun setOnPlaybackPositionUpdateListener(
        listener: RadioPlayerOnPlaybackPositionUpdateListener?
    ) {
        onPlaybackPositionUpdate = listener
    }

    fun setOnFinishListener(
        listener: RadioPlayerOnFinishListener?
    ) {
        onFinish = listener
    }

    private fun createDurationTimer() {
        playbackPositionUpdater = kotlin.concurrent.timer(period = 100L) {
            playbackPosition?.let {
                onPlaybackPositionUpdate?.invoke(it)
            }
        }
    }

    private fun destroyDurationTimer() {
        playbackPositionUpdater?.cancel()
        playbackPositionUpdater = null
    }

    companion object {
        const val MIN_VOLUME = 0f
        const val MAX_VOLUME = 1f
        const val DUCK_VOLUME = 0.2f
    }
}
