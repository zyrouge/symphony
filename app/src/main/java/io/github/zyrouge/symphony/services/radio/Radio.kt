package io.github.zyrouge.symphony.services.radio

import android.media.MediaPlayer
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.SymphonyHooks
import io.github.zyrouge.symphony.utils.Eventer

enum class RadioEvents {
    StartPlaying,
    StopPlaying,
    PausePlaying,
    ResumePlaying,
    SongSeeked,
    SongQueued,
    SongDequeued,
    QueueIndexChanged,
    QueueModified,
    LoopModeChanged,
    ShuffleModeChanged,
    SongStaged,
    QueueEnded,
}

class Radio(private val symphony: Symphony) : SymphonyHooks {
    val onUpdate = Eventer<RadioEvents>()
    val queue = RadioQueue(symphony)
    val shorty = RadioShorty(symphony)

    private var manager = MediaPlayerManager(symphony)
    private val currentMediaPlayer: MediaPlayer?
        get() = manager.getPlayer()
    private var notification = PlayerNotification(symphony)

    val hasPlayer: Boolean
        get() = currentMediaPlayer != null
    val isPlaying: Boolean
        get() = currentMediaPlayer?.isPlaying ?: false
    val currentPlaybackPosition: PlaybackPosition?
        get() = manager.playbackPosition
    val onPlaybackPositionUpdate: Eventer<PlaybackPosition>
        get() = manager.onPlaybackPositionUpdate

    data class PlayOptions(
        val index: Int = 0,
        val autostart: Boolean = true,
        val startPosition: Int? = null,
    )

    fun play(options: PlayOptions) {
        if (hasPlayer) stopCurrentSong()
        if (!queue.hasSongAt(options.index)) return
        val song =
            queue.getSongAt(options.index) ?: return play(options.copy(index = options.index + 1))
        queue.currentSongIndex = options.index
        manager.play(song.uri) { onSongFinish() }
        onUpdate.dispatch(RadioEvents.SongStaged)
        options.startPosition?.let { seek(it) }
        if (options.autostart) {
            currentMediaPlayer!!.start()
            onUpdate.dispatch(RadioEvents.StartPlaying)
        }
    }

    fun pause() {
        currentMediaPlayer?.pause()
        onUpdate.dispatch(RadioEvents.PausePlaying)
    }

    fun resume() {
        currentMediaPlayer?.start()
        onUpdate.dispatch(RadioEvents.ResumePlaying)
    }

    fun stop() {
        if (!hasPlayer) return
        stopCurrentSong()
        queue.reset()
    }

    fun jumpTo(index: Int) = play(PlayOptions(index = index))
    fun jumpToPrevious() = jumpTo(queue.currentSongIndex - 1)
    fun jumpToNext() = jumpTo(queue.currentSongIndex + 1)
    fun canJumpToPrevious() = queue.hasSongAt(queue.currentSongIndex - 1)
    fun canJumpToNext() = queue.hasSongAt(queue.currentSongIndex + 1)

    fun seek(position: Int) {
        if (!hasPlayer) return
        currentMediaPlayer!!.seekTo(position)
        onUpdate.dispatch(RadioEvents.SongSeeked)
    }

    private fun stopCurrentSong() {
        if (!hasPlayer) return
        manager.stop()
        onUpdate.dispatch(RadioEvents.StopPlaying)
    }

    private fun onSongFinish() {
        stopCurrentSong()
        when (queue.currentLoopMode) {
            RadioLoopMode.Song -> play(PlayOptions(queue.currentSongIndex))
            else -> {
                var nextSongIndex = queue.currentSongIndex + 1
                if (!queue.hasSongAt(nextSongIndex) && queue.currentLoopMode == RadioLoopMode.Queue) {
                    nextSongIndex = 0
                }
                if (queue.hasSongAt(nextSongIndex)) {
                    play(PlayOptions(nextSongIndex))
                } else {
                    queue.currentSongIndex = -1
                }
            }
        }
    }

    override fun onSymphonyReady() {
        symphony.settings.getPreviousSongQueue()?.let { previous ->
            var currentSongIndex = previous.currentSongIndex
            var playedDuration = previous.playedDuration
            val originalQueue = mutableListOf<Long>()
            val currentQueue = mutableListOf<Long>()
            previous.originalQueue.forEach { songId ->
                if (symphony.groove.song.hasSongWithId(songId)) {
                    originalQueue.add(songId)
                }
            }
            previous.currentQueue.forEachIndexed { i, songId ->
                if (symphony.groove.song.hasSongWithId(songId)) {
                    currentQueue.add(songId)
                } else {
                    if (i < currentSongIndex) currentSongIndex--
                }
            }
            if (originalQueue.isEmpty()) return
            if (currentSongIndex >= originalQueue.size) {
                currentSongIndex = 0
                playedDuration = 0
            }
            queue.restore(
                RadioQueue.Serialized(
                    currentSongIndex = currentSongIndex,
                    playedDuration = playedDuration,
                    originalQueue = originalQueue,
                    currentQueue = currentQueue
                )
            )
        }
        notification.start()
    }

    override fun onSymphonyPause() {
        if (queue.isEmpty()) return
        symphony.settings.setPreviousSongQueue(
            RadioQueue.Serialized.create(
                queue = queue,
                playbackPosition = currentPlaybackPosition ?: PlaybackPosition.zero
            )
        )
    }

    override fun onSymphonyDestroy() {
        notification.destroy()
    }
}
