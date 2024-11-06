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

class RadioPlayer(val symphony: Symphony, uri: Uri) {
    data class PlaybackPosition(val played: Long, val total: Long) {
        val ratio: Float
            get() = (played.toFloat() / total).takeIf { it.isFinite() } ?: 0f

        companion object {
            val zero = PlaybackPosition(0L, 0L)
        }
    }

    var usable = false
    var hasPlayedOnce = false

    private val unsafeMediaPlayer: MediaPlayer
    private val mediaPlayer: MediaPlayer?
        get() = if (usable) unsafeMediaPlayer else null

    private var onPrepared: RadioPlayerOnPreparedListener? = null
    private var onPlaybackPosition: RadioPlayerOnPlaybackPositionListener? = null
    private var onFinish: RadioPlayerOnFinishListener? = null
    private var onError: RadioPlayerOnErrorListener? = null
    private var playbackPositionUpdater: Timer? = null

    val playbackPosition: PlaybackPosition?
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

    var volume = MAX_VOLUME
    var speed = DEFAULT_SPEED
    var pitch = DEFAULT_PITCH
    val fadePlayback get() = symphony.settings.fadePlayback.value
    val audioSessionId get() = mediaPlayer?.audioSessionId

    private val fadePlaybackDuration get() = (symphony.settings.fadePlaybackDuration.value * 1000).toInt()
    private var fader: RadioEffects.Fader? = null
    val isPlaying get() = mediaPlayer?.isPlaying == true

    init {
        unsafeMediaPlayer = MediaPlayer().also { ump ->
            ump.setOnPreparedListener {
                usable = true
                ump.playbackParams.setAudioFallbackMode(PlaybackParams.AUDIO_FALLBACK_MODE_DEFAULT)
                createDurationTimer()
                onPrepared?.invoke()
            }
            ump.setOnCompletionListener {
                usable = false
                onFinish?.invoke()
            }
            ump.setOnErrorListener { _, what, extra ->
                usable = false
                onError?.invoke(what, extra)
                true
            }
            ump.setDataSource(symphony.applicationContext, uri)
        }
    }

    fun prepare(listener: RadioPlayerOnPreparedListener) {
        onPrepared = listener
        unsafeMediaPlayer.prepareAsync()
    }

    fun stop() {
        usable = false
        destroyDurationTimer()
        symphony.groove.coroutineScope.launch {
            unsafeMediaPlayer.stop()
            unsafeMediaPlayer.release()
        }
    }

    fun start() = mediaPlayer?.let {
        it.start()
        if (!hasPlayedOnce) {
            hasPlayedOnce = true
            setSpeed(speed)
            setPitch(pitch)
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

    @JvmName("setSpeedTo")
    fun setSpeed(to: Float) {
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

    @JvmName("setPitchTo")
    fun setPitch(to: Float) {
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
