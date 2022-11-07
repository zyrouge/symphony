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

class MediaPlayerManager(private val symphony: Symphony) {
    var usable = false
    private var player: MediaPlayer? = null
    private var playbackPositionUpdater: Timer? = null
    val onPlaybackPositionUpdate = Eventer<PlaybackPosition>()

    val playbackPosition: PlaybackPosition?
        get() = if (usable) PlaybackPosition(
            played = player!!.currentPosition,
            total = player!!.duration
        ) else null

    fun play(uri: Uri, onFinish: () -> Unit) {
        player = MediaPlayer.create(symphony.applicationContext, uri)
        player!!.setOnCompletionListener {
            usable = false
            onFinish()
        }
        createDurationTimer()
        usable = true
    }

    fun stop() {
        player?.let {
            destroyDurationTimer()
            it.release()
            usable = false
        }
    }

    fun getPlayer() = if (usable) player else null

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
}