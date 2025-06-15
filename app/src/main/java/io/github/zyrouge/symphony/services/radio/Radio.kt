package io.github.zyrouge.symphony.services.radio

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.entities.Song
import io.github.zyrouge.symphony.utils.Logger
import java.time.Instant
import java.util.Date
import java.util.Timer

class Radio(private val symphony: Symphony) : Symphony.Hooks {
    data class SleepTimer(
        val duration: Long,
        val endsAt: Long,
        val timer: Timer,
        var quitOnEnd: Boolean,
    )

    val queue = RadioQueue(symphony)
    val shorty = RadioShorty(symphony)
    val session = RadioSession(symphony)

    private val focus = RadioFocus(symphony)
    private val nativeReceiver = RadioNativeReceiver(symphony)
    private var player: RadioPlayer? = null
    private var nextPlayer: RadioPlayer? = null

    init {
        nativeReceiver.start()
    }

    fun ready() {
        session.start()
    }

    fun destroy() {
        stop()
        session.destroy()
        nativeReceiver.destroy()
    }

    data class PlayOptions(
        val songMappingId: String? = null,
        val autostart: Boolean = true,
        val startPosition: Long? = null,
    )

    suspend fun play(options: PlayOptions) {
        stopCurrentSong()
        // TODO: can queue be nullable?
        val queue =
            symphony.database.songQueue.findByInternalId(RadioQueue.SONG_QUEUE_INTERNAL_ID_DEFAULT)
        if (queue == null) {
            onSongFinish(SongFinishSource.Exception)
            return
        }
        val song = options.songMappingId
            ?.let { symphony.database.songQueueSongMapping.findById(queue.entity.id, it) }
            ?: symphony.database.songQueueSongMapping.findHead(queue.entity.id)
        if (song == null) {
            onSongFinish(SongFinishSource.Exception)
            return
        }
        try {
            val nQueue = queue.entity.copy(playingId = song.mapping.id)
            symphony.database.songQueue.update(nQueue)
            player = nextPlayer?.takeIf {
                when {
                    it.id == song.entity.id -> true
                    else -> {
                        it.destroy()
                        false
                    }
                }
            } ?: RadioPlayer(symphony, song.entity.id, song.entity.uri)
            nextPlayer = null
            player!!.setOnPreparedListener {
                options.startPosition?.let {
                    if (it > 0L) {
                        seek(it)
                    }
                }
                setSpeed(queue.entity.speed, true)
                setPitch(queue.entity.pitch, true)
                if (options.autostart) {
                    start()
                }
            }
            player!!.setOnPlaybackPositionListener {
                // TODO
                // onPlaybackPositionUpdate.dispatch(it)
            }
            player!!.setOnFinishListener {
                onSongFinish(SongFinishSource.Finish)
            }
            player!!.setOnErrorListener { what, extra ->
                Logger.warn(
                    "Radio",
                    "skipping song ${song.entity.id} (${song.mapping.id}) due to $what + $extra"
                )
                when {
                    // happens when change playback params fail, we skip it since its non-critical
                    what == 1 && extra == -22 -> onSongFinish(SongFinishSource.Finish)
                    else -> {
                        removeFromQueue(queue.entity.id, song)
                        onSongFinish(SongFinishSource.Exception)
                    }
                }
            }
            player!!.prepare()
            prepareNextPlayer()
        } catch (err: Exception) {
            Logger.warn(
                "Radio",
                "skipping song ${song.entity.id} (${song.mapping.id})",
                err,
            )
            removeFromQueue(queue.entity.id, song)
        }
    }

    private suspend fun removeFromQueue(queueId: String, song: Song.AlongSongQueueMapping) {
        val previousSong =
            symphony.database.songQueueSongMapping.findByNextId(queueId, song.mapping.id)
        if (previousSong == null) {
            // TODO: handle this
            return
        }
        val nPreviousSongMapping = previousSong.mapping.copy(
            nextId = song.mapping.nextId,
            ogNextId = song.mapping.ogNextId,
        )
        symphony.database.songQueueSongMapping.delete(queueId, listOf(song.mapping.id))
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
            }
        }
    }

    fun pauseInstant() {
        player?.pause()
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
}
