package io.github.zyrouge.symphony.services.radio

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.SymphonyHooks
import io.github.zyrouge.symphony.utils.Eventer
import io.github.zyrouge.symphony.utils.Logger
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.*
import kotlin.math.max

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
    QueueCleared,
    QueueEnded,
    SleepTimerSet,
    SleepTimerRemoved,
}

data class RadioSleepTimer(
    val duration: Long,
    val endsAt: Long,
    val timer: Timer,
    var quitOnEnd: Boolean,
)

class Radio(private val symphony: Symphony) : SymphonyHooks {
    val onUpdate = Eventer<RadioEvents>()
    val queue = RadioQueue(symphony)
    val shorty = RadioShorty(symphony)
    val session = RadioSession(symphony)

    private val focus = RadioFocus(symphony)
    private val nativeReceiver = RadioNativeReceiver(symphony)

    private var player: RadioPlayer? = null
    private var focusCounter = 0
    private var sleepTimer: RadioSleepTimer? = null

    val hasPlayer: Boolean
        get() = player?.usable ?: false
    val isPlaying: Boolean
        get() = player?.isPlaying ?: false
    val currentPlaybackPosition: PlaybackPosition?
        get() = player?.playbackPosition
    val onPlaybackPositionUpdate = Eventer<PlaybackPosition>()

    init {
        nativeReceiver.start()
    }

    fun destroy() {
        stop()
        session.destroy()
        nativeReceiver.destroy()
    }

    data class PlayOptions(
        val index: Int = 0,
        val autostart: Boolean = true,
        val startPosition: Int? = null,
    )

    fun play(options: PlayOptions) {
        stopCurrentSong()
        if (!queue.hasSongAt(options.index)) {
            queue.currentSongIndex = -1
            return
        }
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
            player!!.prepare {
                options.startPosition?.let { seek(it) }
                if (options.autostart) {
                    start(0)
                    onUpdate.dispatch(RadioEvents.StartPlaying)
                }
            }
        } catch (err: Exception) {
            Logger.warn(
                "Radio",
                "skipping song at ${queue.currentPlayingSong} (${queue.currentSongIndex}) due to $err"
            )
            queue.remove(queue.currentSongIndex)
        }
    }

    fun resume() = start(1)
    private fun start(source: Int) {
        player?.let {
            val hasFocus = requestFocus()
            if (hasFocus || !symphony.settings.getRequireAudioFocus()) {
                if (it.fadePlayback) {
                    it.setVolumeInstant(RadioPlayer.MIN_VOLUME)
                }
                it.setVolume(RadioPlayer.MAX_VOLUME) {}
                it.start()
                onUpdate.dispatch(
                    when (source) {
                        0 -> RadioEvents.StartPlaying
                        else -> RadioEvents.ResumePlaying
                    }
                )
            }
        }
    }

    fun pause() = pause {}
    private fun pause(forceFade: Boolean = false, onFinish: () -> Unit) {
        player?.let {
            it.setVolume(
                to = RadioPlayer.MIN_VOLUME,
                forceFade = forceFade,
            ) { _ ->
                it.pause()
                abandonFocus()
                onFinish()
                onUpdate.dispatch(RadioEvents.PausePlaying)
            }
        }
    }

    fun pauseInstant() {
        player?.let {
            it.pause()
            onUpdate.dispatch(RadioEvents.PausePlaying)
        }
    }

    fun stop(ended: Boolean = true) {
        stopCurrentSong()
        queue.reset()
        clearSleepTimer()
        if (ended) onUpdate.dispatch(RadioEvents.QueueEnded)
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

    fun duck() {
        player?.let {
            it.setVolume(RadioPlayer.DUCK_VOLUME) {}
        }
    }

    fun restoreVolume() {
        player?.let {
            it.setVolume(RadioPlayer.MAX_VOLUME) {}
        }
    }

    fun getSleepTimer() = sleepTimer
    fun hasSleepTimer() = sleepTimer != null

    fun setSleepTimer(
        duration: Long,
        quitOnEnd: Boolean,
    ) {
        val endsAt = System.currentTimeMillis() + duration
        val timer = Timer()
        timer.schedule(
            kotlin.concurrent.timerTask {
                val shouldQuit = getSleepTimer()?.quitOnEnd ?: quitOnEnd
                clearSleepTimer()
                pause(forceFade = true) {
                    if (shouldQuit) {
                        symphony.closeApp?.invoke()
                    }
                }
            },
            Date.from(Instant.ofEpochMilli(endsAt)),
        )
        clearSleepTimer()
        sleepTimer = RadioSleepTimer(
            duration = duration,
            endsAt = endsAt,
            timer = timer,
            quitOnEnd = quitOnEnd,
        )
        onUpdate.dispatch(RadioEvents.SleepTimerSet)
    }

    fun clearSleepTimer() {
        sleepTimer?.timer?.cancel()
        sleepTimer = null
        onUpdate.dispatch(RadioEvents.SleepTimerRemoved)
    }

    private fun stopCurrentSong() {
        player?.let {
            player = null
            it.setOnPlaybackPositionUpdateListener {}
            it.setVolume(RadioPlayer.MIN_VOLUME) { _ ->
                it.stop()
                onUpdate.dispatch(RadioEvents.StopPlaying)
            }
        }
    }

    private fun onSongFinish() {
        stopCurrentSong()
        when (queue.currentLoopMode) {
            RadioLoopMode.Song -> play(PlayOptions(queue.currentSongIndex))
            else -> {
                var autostart = true
                var nextSongIndex = queue.currentSongIndex + 1
                if (!queue.hasSongAt(nextSongIndex)) {
                    nextSongIndex = 0
                    autostart = queue.currentLoopMode == RadioLoopMode.Queue
                }
                if (queue.hasSongAt(nextSongIndex)) {
                    play(PlayOptions(nextSongIndex, autostart = autostart))
                } else {
                    queue.reset()
                }
            }
        }
    }

    private fun requestFocus(): Boolean {
        val result = focus.requestFocus()
        if (result) {
            focusCounter++
        }
        return result
    }

    private fun abandonFocus() {
        focusCounter = max(0, focusCounter - 1)
        if (focusCounter == 0) {
            focus.abandonFocus()
        }
    }

    private fun attachGrooveListener() {
        symphony.groove.coroutineScope.launch {
            symphony.groove.readyDeferred.await()
            restorePreviousQueue()
        }
    }

    private fun restorePreviousQueue() {
        if (!queue.isEmpty()) return
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
            if (originalQueue.isEmpty() || hasPlayer) return@let
            if (currentSongIndex >= originalQueue.size) {
                currentSongIndex = 0
                playedDuration = 0
            }
            queue.restore(
                RadioQueue.Serialized(
                    currentSongIndex = currentSongIndex,
                    playedDuration = playedDuration,
                    originalQueue = originalQueue,
                    currentQueue = currentQueue,
                    shuffled = previous.shuffled,
                )
            )
        }
    }

    override fun onSymphonyReady() {
        attachGrooveListener()
        session.start()
    }

    override fun onSymphonyPause() {
        saveCurrentQueue()
    }

    override fun onSymphonyDestroy() {
        saveCurrentQueue()
        destroy()
    }

    private fun saveCurrentQueue() {
        if (queue.isEmpty()) return
        symphony.settings.setPreviousSongQueue(
            RadioQueue.Serialized.create(
                queue = queue,
                playbackPosition = currentPlaybackPosition ?: PlaybackPosition.zero
            )
        )
    }
}
