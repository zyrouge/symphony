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
        get() = played.toFloat() / total.toFloat()

    companion object {
        val zero = PlaybackPosition(0, 0)
    }
}

typealias RadioPlayerOnPlaybackPositionUpdateListener = (PlaybackPosition) -> Unit
typealias RadioPlayerOnFinishListener = () -> Unit

class RadioPlayer(val symphony: Symphony, val uri: Uri) {
    var usable = false
    private val unsafeMediaPlayer: MediaPlayer
    private val mediaPlayer: MediaPlayer?
        get() = if (usable) unsafeMediaPlayer else null

    private var onPlaybackPositionUpdate: RadioPlayerOnPlaybackPositionUpdateListener? = null
    private var onFinish: RadioPlayerOnFinishListener? = null
    private var playbackPositionUpdater: Timer? = null
    val playbackPosition: PlaybackPosition?
        get() = mediaPlayer?.let {
            PlaybackPosition(
                played = it.currentPosition,
                total = it.duration
            )
        }

    var volume: Float = MAX_VOLUME
    val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying ?: false

    init {
        unsafeMediaPlayer = MediaPlayer.create(symphony.applicationContext, uri).apply {
            setOnCompletionListener {
                usable = false
                onFinish?.invoke()
            }
            setOnErrorListener { _, _, _ ->
                true
            }
        }
        createDurationTimer()
        usable = true
    }

    fun stop() {
        usable = false
        destroyDurationTimer()
        mediaPlayer?.let {
            it.stop()
            it.release()
        }
    }

    fun start() = mediaPlayer?.start()
    fun pause() = mediaPlayer?.pause()
    fun seek(to: Int) = mediaPlayer?.seekTo(to)

    @JvmName("setVolumeTo")
    fun setVolume(to: Float) {
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
    }
}
