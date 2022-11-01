package io.github.zyrouge.symphony.services

import android.media.MediaPlayer
import android.net.Uri
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.utils.Eventer
import java.util.*

enum class PlayerEvent {
    StartPlaying,
    StopPlaying,
    PausePlaying,
    ResumePlaying,
    Seeked,
    SongQueued,
    SongDequeued,
    QueueIndexChanged,
    QueueModified,
    SongEnded,
    LoopModeChanged,
    QueueEnded,
}

data class PlayerDuration(
    val played: Int,
    val total: Int,
) {
    fun toRatio() = played.toFloat() / total.toFloat()
    fun toPercent() = toRatio() * 100

    companion object {
        val zero = PlayerDuration(0, 0)
    }
}

enum class LoopMode {
    None,
    Queue,
    Song;

    companion object {
        val all = values()
    }
}

class MediaPlayerManager(private val symphony: Symphony) {
    var usable = false
    private var player: MediaPlayer? = null

    fun play(uri: Uri, onFinish: () -> Unit) {
        player = MediaPlayer.create(symphony.applicationContext, uri)
        player!!.setOnCompletionListener {
            usable = false
            onFinish()
        }
        usable = true
    }

    fun release() {
        player?.let {
            it.release()
            usable = false
        }
    }

    fun getPlayer() = if (usable) player else null
}

class Player(private val symphony: Symphony) {
    val onUpdate = Eventer<PlayerEvent>()
    val onDurationUpdate = Eventer<PlayerDuration>()
    var queue = mutableListOf<Song>()
    var currentLoopMode = LoopMode.None
    var currentSongIndex = -1

    val currentPlayingSong: Song?
        get() = if (currentSongIndex != -1) queue[currentSongIndex] else null

    private var currentMediaPlayerManager = MediaPlayerManager(symphony)
    private val currentMediaPlayer: MediaPlayer?
        get() = currentMediaPlayerManager.getPlayer()
    private var durationTimer: Timer? = null

    val hasPlayer: Boolean
        get() = currentMediaPlayer != null
    val isPlaying: Boolean
        get() = currentMediaPlayer?.isPlaying ?: false
    val duration: PlayerDuration?
        get() = if (hasPlayer) PlayerDuration(
            played = currentMediaPlayer!!.currentPosition,
            total = currentMediaPlayer!!.duration
        ) else null

    fun play(index: Int) {
        if (hasPlayer) stopCurrentSong()
        if (!hasSongAt(index)) return
        val song = queue[index]
        currentSongIndex = index
        currentMediaPlayerManager.play(song.uri) {
            onSongFinish()
        }
        currentMediaPlayer!!.start()
        onUpdate.dispatch(PlayerEvent.StartPlaying)
        createDurationTimer()
    }

    fun pause() {
        currentMediaPlayer?.pause()
        onUpdate.dispatch(PlayerEvent.PausePlaying)
    }

    fun resume() {
        currentMediaPlayer?.start()
        onUpdate.dispatch(PlayerEvent.ResumePlaying)
    }

    fun stop() {
        if (!hasPlayer) return
        stopCurrentSong()
        queue.clear()
        currentSongIndex = -1
        onUpdate.dispatch(PlayerEvent.StopPlaying)
    }

    fun jumpTo(index: Int) = play(index)
    fun jumpToPrevious() = jumpTo(currentSongIndex - 1)
    fun jumpToNext() = jumpTo(currentSongIndex + 1)

    fun hasSongAt(index: Int) = index > -1 && index < queue.size
    fun canJumpToPrevious() = hasSongAt(currentSongIndex - 1)
    fun canJumpToNext() = hasSongAt(currentSongIndex + 1)

    fun seek(position: Int) {
        if (!hasPlayer) return
        currentMediaPlayer!!.seekTo(position)
        onUpdate.dispatch(PlayerEvent.Seeked)
    }

    fun addToQueue(songs: List<Song>, index: Int? = null) {
        index?.let {
            queue.addAll(it, songs)
            if (it <= currentSongIndex) {
                currentSongIndex += songs.size
                onUpdate.dispatch(PlayerEvent.QueueIndexChanged)
            }
        } ?: run {
            queue.addAll(songs)
        }
        afterAddToQueue()
    }

    fun addToQueue(song: Song, index: Int? = null) = addToQueue(listOf(song), index)

    fun removeFromQueue(index: Int) {
        queue.removeAt(index)
        onUpdate.dispatch(PlayerEvent.SongDequeued)
        if (currentSongIndex == index) {
            play(currentSongIndex)
        } else if (index < currentSongIndex) {
            currentSongIndex--
            onUpdate.dispatch(PlayerEvent.QueueIndexChanged)
        }
    }

    fun removeFromQueue(indices: List<Int>) {
        var deflection = 0
        var currentSongRemoved = false
        for (i in indices) {
            queue.removeAt(i - deflection)
            if (i <= currentSongIndex) {
                if (i == currentSongIndex) currentSongRemoved = true
                deflection++
            }
        }
        currentSongIndex -= deflection
        onUpdate.dispatch(PlayerEvent.QueueModified)
        if (currentSongRemoved) {
            play(currentSongIndex)
        }
    }

    fun setLoopMode(loopMode: LoopMode) {
        currentLoopMode = loopMode
        onUpdate.dispatch(PlayerEvent.LoopModeChanged)
    }

    fun toggleLoopMode() {
        val next = LoopMode.all.indexOf(currentLoopMode) + 1
        setLoopMode(LoopMode.all[if (next < LoopMode.all.size) next else 0])
    }

    private fun afterAddToQueue() {
        if (!hasPlayer) {
            play(0)
        }
    }

    private fun stopCurrentSong() {
        if (!hasPlayer) return
        destroyDurationTimer()
        currentMediaPlayer!!.stop()
        currentMediaPlayerManager.release()
    }

    private fun createDurationTimer() {
        durationTimer = kotlin.concurrent.timer(period = 100L) {
            if (!hasPlayer) return@timer
            val currentDuration = duration
            currentDuration?.let {
                onDurationUpdate.dispatch(it)
            }
        }
    }

    private fun destroyDurationTimer() {
        durationTimer?.cancel()
        durationTimer = null
    }

    private fun onSongFinish() {
        onUpdate.dispatch(PlayerEvent.SongEnded)
        when (currentLoopMode) {
            LoopMode.Song -> play(currentSongIndex)
            else -> {
                var nextSongIndex = currentSongIndex + 1
                if (!hasSongAt(nextSongIndex) && currentLoopMode == LoopMode.Queue) {
                    nextSongIndex = 0
                }
                if (hasSongAt(nextSongIndex)) {
                    play(nextSongIndex)
                } else {
                    currentSongIndex = -1
                    onUpdate.dispatch(PlayerEvent.QueueEnded)
                }
            }
        }
    }
}