package io.github.zyrouge.symphony.services

import android.media.MediaPlayer
import android.util.Log
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

class Player(private val symphony: Symphony) {
    val onUpdate = Eventer<PlayerEvent>()
    val onDurationUpdate = Eventer<PlayerDuration>()
    var queue = mutableListOf<Song>()
    var currentSongIndex = -1

    val currentPlayingSong: Song?
        get() = if (currentSongIndex != -1) queue[currentSongIndex] else null

    private var currentMediaPlayer: MediaPlayer? = null
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
        if (!hasSongAt(index)) {
            Log.e("SymphonyPlayer", "Invalid index $index (queue size: ${queue.size})")
            return
        }
        val song = queue[index]
        currentSongIndex = index
        currentMediaPlayer = MediaPlayer.create(symphony.applicationContext, song.uri)
        currentMediaPlayer!!.setOnCompletionListener {
            onSongFinish()
        }
        currentMediaPlayer!!.start()
        onUpdate.dispatch(PlayerEvent.StartPlaying)
        createDurationTimer()
    }

    fun pause() {
        currentMediaPlayer?.pause()
        destroyDurationTimer()
        onUpdate.dispatch(PlayerEvent.PausePlaying)
    }

    fun resume() {
        currentMediaPlayer?.start()
        onUpdate.dispatch(PlayerEvent.ResumePlaying)
        createDurationTimer()
    }

    fun stop() {
        if (!hasPlayer) return
        currentMediaPlayer!!.stop()
        currentMediaPlayer!!.release()
        currentMediaPlayer = null
        queue.clear()
        currentSongIndex = -1
        onUpdate.dispatch(PlayerEvent.StopPlaying)
        destroyDurationTimer()
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

    fun addToQueue(songs: List<Song>) {
        queue.addAll(songs)
        onUpdate.dispatch(PlayerEvent.SongQueued)
        afterAddToQueue()
    }

    fun addToQueue(song: Song) {
        queue.add(song)
        onUpdate.dispatch(PlayerEvent.SongQueued)
        afterAddToQueue()
    }

    fun addToQueue(song: Song, index: Int) {
        queue.add(index, song)
        onUpdate.dispatch(PlayerEvent.SongQueued)
        afterAddToQueue()
    }

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

    private fun afterAddToQueue() {
        if (!hasPlayer) {
            play(0)
        }
    }

    private fun stopCurrentSong() {
        if (!hasPlayer) return
        currentMediaPlayer?.stop()
        currentMediaPlayer?.release()
        currentMediaPlayer = null
    }

    private fun createDurationTimer() {
        durationTimer = kotlin.concurrent.timer(period = 100L) {
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
        val nextSongIndex = currentSongIndex + 1
        if (hasSongAt(nextSongIndex)) {
            play(nextSongIndex)
        } else {
            currentSongIndex = -1
        }
    }
}