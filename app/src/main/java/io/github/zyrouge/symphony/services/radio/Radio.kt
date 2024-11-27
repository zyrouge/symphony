package io.github.zyrouge.symphony.services.radio

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.Eventer
import io.github.zyrouge.symphony.utils.Logger
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.Date
import java.util.Timer

class Radio(private val symphony: Symphony) : Symphony.Hooks {
    sealed class Events {
        sealed class Player : Events() {
            object Staged : Player()
            object Started : Player()
            object Stopped : Player()
            object Paused : Player()
            object Resumed : Player()
            object Seeked : Player()
            object Ended : Player()
        }

        sealed class Queue : Events() {
            object Modified : Queue()
            object IndexChanged : Queue()
            object Cleared : Queue()
        }

        sealed class QueueOption : Events() {
            object LoopModeChanged : QueueOption()
            object ShuffleModeChanged : QueueOption()
            object SleepTimerChanged : QueueOption()
            object SpeedChanged : QueueOption()
            object PitchChanged : QueueOption()
            object PauseOnCurrentSongEndChanged : QueueOption()
        }
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
    private var nextPlayer: RadioPlayer? = null

    val hasPlayer get() = player?.usable == true
    val isPlaying get() = player?.isPlaying == true
    val currentPlaybackPosition get() = player?.playbackPosition
    val currentSpeed get() = player?.speed ?: RadioPlayer.DEFAULT_SPEED
    val currentPitch get() = player?.pitch ?: RadioPlayer.DEFAULT_PITCH
    val audioSessionId get() = player?.audioSessionId
    val onPlaybackPositionUpdate = Eventer<RadioPlayer.PlaybackPosition>()

    var persistedSpeed = RadioPlayer.DEFAULT_SPEED
    var persistedPitch = RadioPlayer.DEFAULT_PITCH
    var sleepTimer: SleepTimer? = null
    var pauseOnCurrentSongEnd = false

    init {
        nativeReceiver.start()
        onUpdate.subscribe(this::watchQueueUpdates)
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
            player = nextPlayer?.takeIf {
                when {
                    it.id == song.id -> true
                    else -> {
                        it.destroy()
                        false
                    }
                }
            } ?: RadioPlayer(symphony, song.id, song.uri)
            nextPlayer = null
            player!!.setOnPreparedListener {
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
            player!!.setOnPlaybackPositionListener {
                onPlaybackPositionUpdate.dispatch(it)
            }
            player!!.setOnFinishListener {
                onSongFinish(SongFinishSource.Finish)
            }
            player!!.setOnErrorListener { what, extra ->
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
            player!!.prepare()
            prepareNextPlayer()
            onUpdate.dispatch(Events.Player.Staged)
        } catch (err: Exception) {
            Logger.warn(
                "Radio",
                "skipping song ${queue.currentSongId} (${queue.currentSongIndex})",
                err,
            )
            queue.remove(queue.currentSongIndex)
        }
    }

    private fun prepareNextPlayer() {
        if (!symphony.settings.gaplessPlayback.value) {
            return
        }
        val (nextSongIndex) = getNextSong(SongFinishSource.Finish)
        val song = queue.getSongIdAt(nextSongIndex)?.let { symphony.groove.song.get(it) } ?: return
        if (song.id == nextPlayer?.id) {
            return
        }
        try {
            nextPlayer?.destroy()
            nextPlayer = RadioPlayer(symphony, song.id, song.uri).also {
                it.prepare()
            }
        } catch (err: Exception) {
            Logger.warn(
                "Radio",
                "unable to prepare next player ${song.id} (${nextSongIndex})",
                err,
            )
        }
    }

    fun resume() = start()

    private fun start() {
        player?.let {
            val hasFocus = focus.requestFocus()
            if (symphony.settings.requireAudioFocus.value && !hasFocus) {
                return
            }
            if (it.fadePlayback) {
                it.changeVolumeInstant(RadioPlayer.MIN_VOLUME)
            }
            it.changeVolume(RadioPlayer.MAX_VOLUME) {}
            it.start()
            onUpdate.dispatch(
                when {
                    !it.hasPlayedOnce -> Events.Player.Started
                    else -> Events.Player.Resumed
                }
            )
        }
    }

    fun pause() = pause {}

    private fun pause(forceFade: Boolean = false, onFinish: () -> Unit) {
        player?.let {
            if (!it.isPlaying) {
                return@let
            }
            it.changeVolume(
                to = RadioPlayer.MIN_VOLUME,
                forceFade = forceFade,
            ) { _ ->
                it.pause()
                focus.abandonFocus()
                onFinish()
                onUpdate.dispatch(Events.Player.Paused)
            }
        }
    }

    fun pauseInstant() {
        player?.let {
            it.pause()
            onUpdate.dispatch(Events.Player.Paused)
        }
    }

    fun stop(ended: Boolean = true) {
        stopCurrentSong()
        queue.reset()
        clearSleepTimer()
        persistedSpeed = RadioPlayer.DEFAULT_SPEED
        persistedPitch = RadioPlayer.DEFAULT_PITCH
        if (ended) onUpdate.dispatch(Events.Player.Ended)
    }

    fun jumpTo(index: Int) = play(PlayOptions(index = index))
    fun jumpToPrevious() = jumpTo(queue.currentSongIndex - 1)
    fun jumpToNext() = jumpTo(queue.currentSongIndex + 1)
    fun canJumpToPrevious() = queue.hasSongAt(queue.currentSongIndex - 1)
    fun canJumpToNext() = queue.hasSongAt(queue.currentSongIndex + 1)

    fun seek(position: Long) {
        player?.let {
            it.seek(position.toInt())
            onUpdate.dispatch(Events.Player.Seeked)
        }
    }

    fun duck() {
        player?.let {
            it.changeVolume(RadioPlayer.DUCK_VOLUME) {}
        }
    }

    fun restoreVolume() {
        player?.let {
            it.changeVolume(RadioPlayer.MAX_VOLUME) {}
        }
    }

    fun setSpeed(speed: Float, persist: Boolean) {
        player?.let {
            it.changeSpeed(speed)
            if (persist) {
                persistedSpeed = speed
            }
            onUpdate.dispatch(Events.QueueOption.SpeedChanged)
        }
    }

    fun setPitch(pitch: Float, persist: Boolean) {
        player?.let {
            it.changePitch(pitch)
            if (persist) {
                persistedPitch = pitch
            }
            onUpdate.dispatch(Events.QueueOption.PitchChanged)
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
        onUpdate.dispatch(Events.QueueOption.SleepTimerChanged)
    }

    fun clearSleepTimer() {
        sleepTimer?.timer?.cancel()
        sleepTimer = null
        onUpdate.dispatch(Events.QueueOption.SleepTimerChanged)
    }

    @JvmName("setPauseOnCurrentSongEndTo")
    fun setPauseOnCurrentSongEnd(value: Boolean) {
        pauseOnCurrentSongEnd = value
        onUpdate.dispatch(Events.QueueOption.PauseOnCurrentSongEndChanged)
    }

    private fun stopCurrentSong() {
        player?.let {
            player = null
            it.setOnPlaybackPositionListener {}
            it.changeVolume(RadioPlayer.MIN_VOLUME) { _ ->
                it.stop()
                onUpdate.dispatch(Events.Player.Stopped)
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
        var (nextSongIndex, autostart) = getNextSong(source)
        if (pauseOnCurrentSongEnd) {
            autostart = false
            setPauseOnCurrentSongEnd(false)
        }
        play(PlayOptions(nextSongIndex, autostart = autostart))
    }

    private fun getNextSong(source: SongFinishSource): Pair<Int, Boolean> {
        if (queue.isEmpty()) {
            return -1 to false
        }
        var autostart: Boolean
        var nextSongIndex: Int
        when (queue.currentLoopMode) {
            RadioQueue.LoopMode.Song -> {
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
                    autostart = queue.currentLoopMode == RadioQueue.LoopMode.Queue
                }
            }
        }
        return nextSongIndex to autostart
    }

    private fun attachGrooveListener() {
        symphony.groove.coroutineScope.launch {
            symphony.groove.readyDeferred.await()
            restorePreviousQueue()
        }
    }

    private fun restorePreviousQueue() {
        if (!queue.isEmpty()) {
            return
        }
        symphony.settings.previousSongQueue.value?.let { previous ->
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
            if (originalQueue.isEmpty() || hasPlayer) {
                return@let
            }
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

    internal fun watchQueueUpdates(event: Events) {
        if (event !is Events.Queue) {
            return
        }
        prepareNextPlayer()
    }

    override fun onSymphonyReady() {
        ready()
    }

    override fun onSymphonyDestroy() {
        saveCurrentQueue()
        destroy()
    }

    override fun onSymphonyActivityPause() {
        saveCurrentQueue()
    }

    override fun onSymphonyActivityDestroy() {
        saveCurrentQueue()
    }

    private fun saveCurrentQueue() {
        if (queue.isEmpty()) {
            return
        }
        symphony.settings.previousSongQueue.setValue(
            RadioQueue.Serialized.create(
                queue = queue,
                playbackPosition = currentPlaybackPosition ?: RadioPlayer.PlaybackPosition.zero
            )
        )
    }
}
