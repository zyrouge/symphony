package io.github.zyrouge.symphony.services.radio

import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import io.github.zyrouge.symphony.Symphony
import java.util.Timer

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
typealias RadioPlayerOnPlaybackPositionListener = (PlaybackPosition) -> Unit
typealias RadioPlayerOnFinishListener = () -> Unit
typealias RadioPlayerOnErrorListener = (Int, Int) -> Unit

class RadioPlayer(val symphony: Symphony, uri: Uri) {
    var usable = false
    var hasPlayedOnce = false

    private val unsafeMediaPlayer: MediaPlayer
    private val mediaPlayer: MediaPlayer?
        get() = if (usable) unsafeMediaPlayer else null

    private var initialSpeed: Float? = null
    private var initialPitch: Float? = null
    private var onPrepared: RadioPlayerOnPreparedListener? = null
    private var onPlaybackPosition: RadioPlayerOnPlaybackPositionListener? = null
    private var onFinish: RadioPlayerOnFinishListener? = null
    private var onError: RadioPlayerOnErrorListener? = null
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
        get() = symphony.settings.fadePlayback.value
    val speed: Float
        get() = mediaPlayer?.playbackParams?.speed?.takeIf { it != 0f } ?: DEFAULT_SPEED
    val pitch: Float
        get() = mediaPlayer?.playbackParams?.pitch?.takeIf { it != 0f } ?: DEFAULT_PITCH
    val audioSessionId: Int?
        get() = mediaPlayer?.audioSessionId

    private val fadePlaybackDuration: Int
        get() = (symphony.settings.fadePlaybackDuration.value * 1000).toInt()
    private var fader: RadioEffects.Fader? = null
    val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying ?: false

    init {
        unsafeMediaPlayer = MediaPlayer().apply {
            setOnPreparedListener {
                playbackParams.setAudioFallbackMode(PlaybackParams.AUDIO_FALLBACK_MODE_DEFAULT)
                usable = true
                createDurationTimer()
                onPrepared?.invoke()
            }
            setOnCompletionListener {
                usable = false
                onFinish?.invoke()
            }
            setOnErrorListener { _, what, extra ->
                usable = false
                onError?.invoke(what, extra)
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

    fun start() = mediaPlayer?.let {
        it.start()
        if (!hasPlayedOnce) {
            hasPlayedOnce = true
            initialSpeed?.let { speed ->
                setSpeed(speed)
                initialSpeed = null
            }
            initialPitch?.let { pitch ->
                setPitch(pitch)
                initialPitch = null
            }
        }
    }

    fun pause() = mediaPlayer?.pause()
    fun seek(to: Int) = mediaPlayer?.seekTo(to)

    @JvmName("setVolumeTo")
    fun setVolume(
        to: Float,
        forceFade: Boolean = false,
        onFinish: (Boolean) -> Unit,
    ) {
        fader?.stop()
        when {
            to == volume -> onFinish(true)
            forceFade || fadePlayback -> {
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

    fun setSpeed(speed: Float) {
        if (!hasPlayedOnce) {
            initialSpeed = speed
            return
        }
        mediaPlayer?.let {
            val isPlaying = it.isPlaying
            it.playbackParams = it.playbackParams.setSpeed(speed)
            if (!isPlaying) {
                it.pause()
            }
        }
    }

    fun setPitch(pitch: Float) {
        if (!hasPlayedOnce) {
            initialPitch = speed
            return
        }
        mediaPlayer?.let {
            val isPlaying = it.isPlaying
            it.playbackParams = it.playbackParams.setPitch(pitch)
            if (!isPlaying) {
                it.pause()
            }
        }
    }

    fun setOnPlaybackPositionListener(listener: RadioPlayerOnPlaybackPositionListener?) {
        onPlaybackPosition = listener
    }

    fun setOnFinishListener(listener: RadioPlayerOnFinishListener?) {
        onFinish = listener
    }

    fun setOnErrorListener(listener: RadioPlayerOnErrorListener?) {
        onError = listener
    }

    private fun createDurationTimer() {
        playbackPositionUpdater = kotlin.concurrent.timer(period = 100L) {
            playbackPosition?.let {
                onPlaybackPosition?.invoke(it)
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
        const val DEFAULT_SPEED = 1f
        const val DEFAULT_PITCH = 1f
    }
}
