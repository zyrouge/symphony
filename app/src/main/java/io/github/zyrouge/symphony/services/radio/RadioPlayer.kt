package io.github.zyrouge.symphony.services.radio

import android.media.MediaPlayer
import android.net.Uri
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.Eventer
import java.util.*

data class PlaybackPosition(
    val played: Int,
    val total: Int,
) {
    fun toRatio() = played.toFloat() / total.toFloat()
    fun toPercent() = toRatio() * 100

    companion object {
        val zero = PlaybackPosition(0, 0)
    }
}

class RadioPlayer(private val symphony: Symphony) {
    var usable = false
    private var mediaPlayer: MediaPlayer? = null
    private var playbackPositionUpdater: Timer? = null

    val onPlaybackPositionUpdate = Eventer<PlaybackPosition>()
    val playbackPosition: PlaybackPosition?
        get() = when {
            usable -> mediaPlayer?.let {
                PlaybackPosition(
                    played = it.currentPosition,
                    total = it.duration
                )
            }
            else -> null
        }

    var volume: Float = MAX_VOLUME
    val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying ?: false

    fun play(uri: Uri, onFinish: () -> Unit) {
        try {
            mediaPlayer = MediaPlayer.create(symphony.applicationContext, uri)
            mediaPlayer!!.setOnCompletionListener {
                usable = false
                onFinish()
            }
            mediaPlayer!!.setOnErrorListener { _, _, _ ->
                true
            }
            createDurationTimer()
            usable = true
        } catch (err: Exception) {
            stop()
            throw err
        }
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

    private fun createDurationTimer() {
        playbackPositionUpdater = kotlin.concurrent.timer(period = 100L) {
            if (!usable) return@timer
            playbackPosition?.let {
                onPlaybackPositionUpdate.dispatch(it)
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

