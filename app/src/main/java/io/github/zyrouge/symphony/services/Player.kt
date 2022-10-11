package io.github.zyrouge.symphony.services

import android.media.MediaPlayer
import android.util.Log
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.utils.Eventer

enum class PlayerEvent {
    StartPlaying,
    StopPlaying,
    PausePlaying,
    ResumePlaying,
    SongQueued,
}

data class PlayerDuration(
    val played: Int,
    val total: Int,
)

class Player {
    val events = Eventer<PlayerEvent>()
    var queue = mutableListOf<Song>()
    var currentSongIndex = -1

    val currentPlayingSong: Song?
        get() = if (currentSongIndex != -1) queue[currentSongIndex] else null

    private var currentMediaPlayer: MediaPlayer? = null
    val hasPlayer: Boolean
        get() = currentMediaPlayer != null
    val isPlaying: Boolean
        get() = currentMediaPlayer?.isPlaying ?: false
    val duration: PlayerDuration?
        get() = if (hasPlayer) PlayerDuration(
            played = currentMediaPlayer!!.currentPosition,
            total = currentMediaPlayer!!.duration
        ) else null

    fun init() {
    }

    fun play(index: Int) {
        if (hasPlayer) stopCurrentSong()
        val song = queue[index]
        currentSongIndex = index
        currentMediaPlayer = MediaPlayer.create(Symphony.context, song.uri)
        currentMediaPlayer!!.start()
        events.dispatch(PlayerEvent.StartPlaying)
    }

    fun pause() {
        currentMediaPlayer?.pause()
        events.dispatch(PlayerEvent.PausePlaying)
    }

    fun resume() {
        currentMediaPlayer?.start()
        events.dispatch(PlayerEvent.ResumePlaying)
    }

    fun stop() {
        if (!hasPlayer) return
        currentMediaPlayer?.stop()
        currentMediaPlayer?.release()
        currentMediaPlayer = null
        queue.clear()
        currentSongIndex = -1
        events.dispatch(PlayerEvent.StopPlaying)
    }

    fun jumpTo(index: Int) = play(index)
    fun jumpToPrevious() = jumpTo(currentSongIndex - 1)
    fun jumpToNext() = jumpTo(currentSongIndex + 1)

    fun hasSongAt(index: Int) = index > 0 && index < queue.size
    fun canJumpToPrevious() = hasSongAt(currentSongIndex - 1)
    fun canJumpToNext() = hasSongAt(currentSongIndex + 1)

    fun addToQueue(songs: List<Song>) {
        Log.i("player", queue.addAll(songs).toString())
        Log.i("player", "after add ${queue.size} (${songs.size})")
        events.dispatch(PlayerEvent.SongQueued)
        afterAddToQueue()
    }

    fun addToQueue(song: Song) {
        queue.add(song)
        events.dispatch(PlayerEvent.SongQueued)
        afterAddToQueue()
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
}