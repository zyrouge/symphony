package io.github.zyrouge.symphony.services.radio

import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.Logger
import kotlinx.coroutines.launch
import java.util.Timer

typealias RadioPlayerOnPreparedListener = () -> Unit
typealias RadioPlayerOnPlaybackPositionListener = (RadioPlayer.PlaybackPosition) -> Unit
typealias RadioPlayerOnFinishListener = () -> Unit
typealias RadioPlayerOnErrorListener = (Int, Int) -> Unit

class RadioPlayer(val symphony: Symphony, val id: String, val uri: Uri) {
    data class PlaybackPosition(val played: Long, val total: Long) {
        val ratio: Float
            get() = (played.toFloat() / total).takeIf { it.isFinite() } ?: 0f

        companion object {
            val zero = PlaybackPosition(0L, 0L)
        }
    }

    enum class State {
        Unprepared,
        Preparing,
        Prepared,
        Finished,
        Destroyed,
    }

    private val unsafeMediaPlayer: MediaPlayer
    private val mediaPlayer: MediaPlayer? get() = if (usable) unsafeMediaPlayer else null
    private var onPrepared: RadioPlayerOnPreparedListener? = null
    private var onPlaybackPosition: RadioPlayerOnPlaybackPositionListener? = null
    private var onFinish: RadioPlayerOnFinishListener? = null
    private var onError: RadioPlayerOnErrorListener? = null
    private var fader: RadioEffects.Fader? = null
    private var playbackPositionUpdater: Timer? = null

    var state = State.Unprepared
        private set
    var hasPlayedOnce = false
        private set
    var volume = MAX_VOLUME
        private set
    var speed = DEFAULT_SPEED
        private set
    var pitch = DEFAULT_PITCH
        private set

    val usable get() = state == State.Prepared
    val fadePlayback get() = symphony.settings.fadePlayback.value
    val audioSessionId get() = mediaPlayer?.audioSessionId
    val isPlaying get() = mediaPlayer?.isPlaying == true

    val playbackPosition
        get() = mediaPlayer?.let {
            try {
                PlaybackPosition(
                    played = it.currentPosition.toLong(),
                    total = it.duration.toLong(),
                )
            } catch (_: IllegalStateException) {
                null
            }
        }

    init {
        unsafeMediaPlayer = MediaPlayer().also { ump ->
            ump.setOnPreparedListener {
                state = State.Prepared
                ump.playbackParams.setAudioFallbackMode(PlaybackParams.AUDIO_FALLBACK_MODE_DEFAULT)
                createDurationTimer()
                onPrepared?.invoke()
            }
            ump.setOnCompletionListener {
                state = State.Finished
                onFinish?.invoke()
            }
            ump.setOnErrorListener { _, what, extra ->
                state = State.Destroyed
                onError?.invoke(what, extra)
                true
            }
            ump.setDataSource(symphony.applicationContext, uri)
        }
    }

    fun prepare() {
        when (state) {
            State.Unprepared -> {
                unsafeMediaPlayer.prepareAsync()
                state = State.Preparing
            }

            State.Prepared -> onPrepared?.invoke()
            else -> {}
        }
    }

    fun stop() = destroy()

    fun destroy() {
        state = State.Destroyed
        destroyDurationTimer()
        symphony.groove.coroutineScope.launch {
            unsafeMediaPlayer.stop()
            unsafeMediaPlayer.release()
        }
    }

    fun start() = mediaPlayer?.let {
        it.start()
        createDurationTimer()
        if (!hasPlayedOnce) {
            hasPlayedOnce = true
            changeSpeed(speed)
            changePitch(pitch)
        }
    }

    fun pause() = mediaPlayer?.let {
        it.pause()
        destroyDurationTimer()
    }

    fun seek(to: Int) = mediaPlayer?.let {
        it.seekTo(to)
        emitPlaybackPosition()
    }

    fun changeVolume(
        to: Float,
        forceFade: Boolean = false,
        onFinish: (Boolean) -> Unit,
    ) {
        fader?.stop()
        when {
            to == volume -> onFinish(true)
            forceFade || fadePlayback -> {
                val duration = (symphony.settings.fadePlaybackDuration.value * 1000).toInt()
                fader = RadioEffects.Fader(
                    RadioEffects.Fader.Options(volume, to, duration),
                    onUpdate = {
                        changeVolumeInstant(it)
                    },
                    onFinish = {
                        onFinish(it)
                        fader = null
                    }
                )
                fader?.start()
            }

            else -> {
                changeVolumeInstant(to)
                onFinish(true)
            }
        }
    }

    fun changeVolumeInstant(to: Float) {
        volume = to
        mediaPlayer?.setVolume(to, to)
    }

    fun changeSpeed(to: Float) {
        if (!hasPlayedOnce) {
            speed = to
            return
        }
        mediaPlayer?.let {
            val isPlaying = it.isPlaying
            try {
                it.playbackParams = it.playbackParams.setSpeed(to)
                speed = to
            } catch (err: Exception) {
                Logger.error("RadioPlayer", "changing speed failed", err)
            }
            if (!isPlaying) {
                it.pause()
            }
        }
    }

    fun changePitch(to: Float) {
        if (!hasPlayedOnce) {
            pitch = to
            return
        }
        mediaPlayer?.let {
            val isPlaying = it.isPlaying
            try {
                it.playbackParams = it.playbackParams.setPitch(to)
                pitch = to
            } catch (err: Exception) {
                Logger.error("RadioPlayer", "changing pitch failed", err)
            }
            if (!isPlaying) {
                it.pause()
            }
        }
    }

    fun setOnPreparedListener(listener: RadioPlayerOnPreparedListener?) {
        onPrepared = listener
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
            emitPlaybackPosition()
        }
    }

    private fun emitPlaybackPosition() {
        playbackPosition?.let {
            onPlaybackPosition?.invoke(it)
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
