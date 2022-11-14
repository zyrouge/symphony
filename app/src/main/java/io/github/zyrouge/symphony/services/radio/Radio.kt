package io.github.zyrouge.symphony.services.radio

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.SymphonyHooks
import io.github.zyrouge.symphony.utils.Eventer
import io.github.zyrouge.symphony.utils.Logger

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

    private var player: RadioPlayer? = null
    private var notification = RadioNotification(symphony)

    val hasPlayer: Boolean
        get() = player?.usable ?: false
    val isPlaying: Boolean
        get() = player?.isPlaying ?: false
    val currentPlaybackPosition: PlaybackPosition?
        get() = player?.playbackPosition
    val onPlaybackPositionUpdate = Eventer<PlaybackPosition>()

    data class PlayOptions(
        val index: Int = 0,
        val autostart: Boolean = true,
        val startPosition: Int? = null,
    )

    fun play(options: PlayOptions) {
        stopCurrentSong()
        if (!queue.hasSongAt(options.index)) return
        val song = queue.getSongAt(options.index)!!
        queue.currentSongIndex = options.index
        try {
            player = RadioPlayer(symphony, song.uri).apply {
                setOnPlaybackPositionUpdateListener {
                    onPlaybackPositionUpdate.dispatch(it)
                }
                setOnFinishListener {
                    onSongFinish()
                }
            }
            onUpdate.dispatch(RadioEvents.SongStaged)
            options.startPosition?.let { seek(it) }
            if (options.autostart) {
                start()
                onUpdate.dispatch(RadioEvents.StartPlaying)
            }
        } catch (err: Exception) {
            Logger.warn("Skipping song at ${queue.currentPlayingSong} (${queue.currentSongIndex}) due to $err")
            queue.remove(queue.currentSongIndex)
        }
    }

    fun resume() = start()
    private fun start() {
        player?.let {
            when {
                symphony.settings.getFadePlayback() -> {
                    runCatching {
                        RadioEffects.fadeIn(it) {}
                    }
                }
                else -> it.start()
            }
            onUpdate.dispatch(RadioEvents.ResumePlaying)
        }
    }

    fun pause() = pause {}
    private fun pause(onEnd: () -> Unit) {
        player?.let {
            when {
                symphony.settings.getFadePlayback() -> {
                    runCatching {
                        RadioEffects.fadeOut(it) {
                            onEnd()
                            onUpdate.dispatch(RadioEvents.PausePlaying)
                        }
                    }
                }
                else -> {
                    it.pause()
                    onEnd()
                    onUpdate.dispatch(RadioEvents.PausePlaying)
                }
            }
        }
    }


    fun stop() {
        stopCurrentSong()
        queue.reset()
    }

    fun jumpTo(index: Int) = play(PlayOptions(index = index))
    fun jumpToPrevious() = jumpTo(queue.currentSongIndex - 1)
    fun jumpToNext() = jumpTo(queue.currentSongIndex + 1)
    fun canJumpToPrevious() = queue.hasSongAt(queue.currentSongIndex - 1)
    fun canJumpToNext() = queue.hasSongAt(queue.currentSongIndex + 1)

    fun seek(position: Int) {
        player?.let {
            it.seek(position)
            onUpdate.dispatch(RadioEvents.SongSeeked)
        }
    }

    private fun stopCurrentSong() {
        player?.let {
            it.setOnPlaybackPositionUpdateListener {}
            pause {
                it.stop()
                onUpdate.dispatch(RadioEvents.StopPlaying)
            }
            player = null
        }
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
