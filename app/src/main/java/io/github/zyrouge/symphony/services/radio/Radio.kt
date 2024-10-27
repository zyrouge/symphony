package io.github.zyrouge.symphony.services.radio

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.Eventer
import io.github.zyrouge.symphony.utils.Logger
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.Date
import java.util.Timer
import kotlin.math.max

class Radio(private val symphony: Symphony) : Symphony.Hooks {
    enum class Events {
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
        SpeedChanged,
        PitchChanged,
        PauseOnCurrentSongEndChanged,
    }

    data class SleepTimer(
        val duration: Long,
        val endsAt: Long,
        val timer: Timer,
        var quitOnEnd: Boolean,
    )

    val onUpdate = Eventer<Events>()
    val queue = RadioQueue(symphony)
    val shorty = RadioShorty(symphony)
    val session = RadioSession(symphony)
    var observatory = RadioObservatory(symphony)

    private val focus = RadioFocus(symphony)
    private val nativeReceiver = RadioNativeReceiver(symphony)

    private var player: RadioPlayer? = null
    private var focusCounter = 0

    val hasPlayer: Boolean
        get() = player?.usable ?: false
    val isPlaying: Boolean
        get() = player?.isPlaying ?: false
    val currentPlaybackPosition: RadioPlayer.PlaybackPosition?
        get() = player?.playbackPosition
    val currentSpeed: Float
        get() = player?.speed ?: RadioPlayer.DEFAULT_SPEED
    val currentPitch: Float
        get() = player?.pitch ?: RadioPlayer.DEFAULT_PITCH
    val audioSessionId: Int?
        get() = player?.audioSessionId
    val onPlaybackPositionUpdate = Eventer<RadioPlayer.PlaybackPosition>()

    var persistedSpeed: Float = RadioPlayer.DEFAULT_SPEED
    var persistedPitch: Float = RadioPlayer.DEFAULT_PITCH
    var sleepTimer: SleepTimer? = null
    var pauseOnCurrentSongEnd = false

    init {
        nativeReceiver.start()
    }

    fun ready() {
        attachGrooveListener()
        session.start()
        observatory.start()
    }

    fun destroy() {
        stop()
        observatory.destroy()
        session.destroy()
        nativeReceiver.destroy()
    }

    data class PlayOptions(
        val index: Int = 0,
        val autostart: Boolean = true,
        val startPosition: Long? = null,
    )

    fun play(options: PlayOptions) {
        stopCurrentSong()
        val song = queue.getSongIdAt(options.index)?.let { symphony.groove.song.get(it) }
        if (song == null) {
            onSongFinish(SongFinishSource.Exception)
            return
        }
        try {
            queue.currentSongIndex = options.index
            player = RadioPlayer(symphony, song.uri).apply {
                setOnPlaybackPositionListener {
                    onPlaybackPositionUpdate.dispatch(it)
                }
                setOnFinishListener {
                    onSongFinish(SongFinishSource.Finish)
                }
                setOnErrorListener { what, extra ->
                    Logger.warn(
                        "Radio",
                        "skipping song ${queue.currentSongId} (${queue.currentSongIndex}) due to $what + $extra"
                    )
                    when {
                        // happens when change playback params fail, we skip it since its non-critical
                        what == 1 && extra == -22 -> onSongFinish(SongFinishSource.Finish)
                        else -> {
                            queue.remove(queue.currentSongIndex)
                            onSongFinish(SongFinishSource.Exception)
                        }
                    }
                }
            }
            onUpdate.dispatch(Events.SongStaged)
            player!!.prepare {
                options.startPosition?.let {
                    if (it > 0L) {
                        seek(it)
                    }
                }
                setSpeed(persistedSpeed, true)
                setPitch(persistedPitch, true)
                if (options.autostart) {
                    start()
                }
            }
        } catch (err: Exception) {
            Logger.warn(
                "Radio",
                "skipping song ${queue.currentSongId} (${queue.currentSongIndex}) due to $err"
            )
            queue.remove(queue.currentSongIndex)
        }
    }

    fun resume() = start()

    private fun start() {
        player?.let {
            val hasFocus = requestFocus()
            if (hasFocus || !symphony.settings.requireAudioFocus.value) {
                if (it.fadePlayback) {
                    it.setVolumeInstant(RadioPlayer.MIN_VOLUME)
                }
                it.setVolume(RadioPlayer.MAX_VOLUME) {}
                it.start()
                onUpdate.dispatch(
                    when {
                        !it.hasPlayedOnce -> Events.StartPlaying
                        else -> Events.ResumePlaying
                    }
                )
            }
        }
    }

    fun pause() = pause {}
    private fun pause(forceFade: Boolean = false, onFinish: () -> Unit) {
        player?.let {
            if (!it.isPlaying) return@let
            it.setVolume(
                to = RadioPlayer.MIN_VOLUME,
                forceFade = forceFade,
            ) { _ ->
                it.pause()
                abandonFocus()
                onFinish()
                onUpdate.dispatch(Events.PausePlaying)
            }
        }
    }

    fun pauseInstant() {
        player?.let {
            it.pause()
            onUpdate.dispatch(Events.PausePlaying)
        }
    }

    fun stop(ended: Boolean = true) {
        stopCurrentSong()
        queue.reset()
        clearSleepTimer()
        persistedSpeed = RadioPlayer.DEFAULT_SPEED
        persistedPitch = RadioPlayer.DEFAULT_PITCH
        if (ended) onUpdate.dispatch(Events.QueueEnded)
    }

    fun jumpTo(index: Int) = play(PlayOptions(index = index))
    fun jumpToPrevious() = jumpTo(queue.currentSongIndex - 1)
    fun jumpToNext() = jumpTo(queue.currentSongIndex + 1)
    fun canJumpToPrevious() = queue.hasSongAt(queue.currentSongIndex - 1)
    fun canJumpToNext() = queue.hasSongAt(queue.currentSongIndex + 1)

    fun seek(position: Long) {
        player?.let {
            it.seek(position.toInt())
            onUpdate.dispatch(Events.SongSeeked)
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

    fun setSpeed(speed: Float, persist: Boolean) {
        player?.let {
            it.setSpeed(speed)
            if (persist) {
                persistedSpeed = speed
            }
            onUpdate.dispatch(Events.SpeedChanged)
        }
    }

    fun setPitch(pitch: Float, persist: Boolean) {
        player?.let {
            it.setPitch(pitch)
            if (persist) {
                persistedPitch = pitch
            }
            onUpdate.dispatch(Events.PitchChanged)
        }
    }

    fun setSleepTimer(
        duration: Long,
        quitOnEnd: Boolean,
    ) {
        val endsAt = System.currentTimeMillis() + duration
        val timer = Timer()
        timer.schedule(
            kotlin.concurrent.timerTask {
                val shouldQuit = sleepTimer?.quitOnEnd ?: quitOnEnd
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
        sleepTimer = SleepTimer(
            duration = duration,
            endsAt = endsAt,
            timer = timer,
            quitOnEnd = quitOnEnd,
        )
        onUpdate.dispatch(Events.SleepTimerSet)
    }

    fun clearSleepTimer() {
        sleepTimer?.timer?.cancel()
        sleepTimer = null
        onUpdate.dispatch(Events.SleepTimerRemoved)
    }

    @JvmName("setPauseOnCurrentSongEndTo")
    fun setPauseOnCurrentSongEnd(value: Boolean) {
        pauseOnCurrentSongEnd = value
        onUpdate.dispatch(Events.PauseOnCurrentSongEndChanged)
    }

    private fun stopCurrentSong() {
        player?.let {
            player = null
            it.setOnPlaybackPositionListener {}
            it.setVolume(RadioPlayer.MIN_VOLUME) { _ ->
                it.stop()
                onUpdate.dispatch(Events.StopPlaying)
            }
        }
    }

    private enum class SongFinishSource {
        Finish,
        Exception,
    }

    private fun onSongFinish(source: SongFinishSource) {
        stopCurrentSong()
        if (queue.isEmpty()) {
            queue.currentSongIndex = -1
            return
        }
        var autostart: Boolean
        var nextSongIndex: Int
        when (queue.currentLoopMode) {
            RadioLoopMode.Song -> {
                nextSongIndex = queue.currentSongIndex
                autostart = source == SongFinishSource.Finish
                if (!queue.hasSongAt(nextSongIndex)) {
                    nextSongIndex = 0
                    autostart = false
                }
            }

            else -> {
                nextSongIndex = when (source) {
                    SongFinishSource.Finish -> queue.currentSongIndex + 1
                    SongFinishSource.Exception -> queue.currentSongIndex
                }
                autostart = true
                if (!queue.hasSongAt(nextSongIndex)) {
                    nextSongIndex = 0
                    autostart = queue.currentLoopMode == RadioLoopMode.Queue
                }
            }
        }
        if (pauseOnCurrentSongEnd) {
            autostart = false
            setPauseOnCurrentSongEnd(false)
        }
        play(PlayOptions(nextSongIndex, autostart = autostart))
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
            val originalQueue = mutableListOf<String>()
            val currentQueue = mutableListOf<String>()
            previous.originalQueue.forEach { songId ->
                if (symphony.groove.song.get(songId) != null) {
                    originalQueue.add(songId)
                }
            }
            previous.currentQueue.forEachIndexed { i, songId ->
                if (symphony.groove.song.get(songId) != null) {
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
        ready()
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
                playbackPosition = currentPlaybackPosition ?: RadioPlayer.PlaybackPosition.zero
            )
        )
    }
}
