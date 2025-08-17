package io.github.zyrouge.symphony.services.radio

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.SymphonyHooks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.Date
import java.util.Timer

class Radio(private val symphony: Symphony) : SymphonyHooks {
    data class SleepTimer(
        val duration: Long,
        val endsAt: Long,
        val quitOnEnd: Boolean,
    )

    private val queue = RadioQueue(symphony)
    private val player = RadioPlayer(symphony)

    private var _sleepTimerTimer: Timer? = null
    private val _sleepTimer = MutableStateFlow<SleepTimer?>(null)
    val sleepTimer = _sleepTimer.asStateFlow()
    private val _pauseOnCurrentSongEnd = MutableStateFlow(false)
    val pauseOnCurrentSongEnd = _pauseOnCurrentSongEnd.asStateFlow()

    val isPlaying get() = player.isPlaying
    val playbackPosition get() = player.playbackPosition
    val volume get() = player.volume
    val speed get() = player.speed
    val pitch get() = player.pitch

    suspend fun play(): Boolean {
        if (player.hasMedia()) {
            player.play()
            return true
        }
        val songQueue = queue.getCurrentSongQueue() ?: return false
        val song = symphony.database.songQueueSongMapping.findHead(songQueue.entity.id)
            ?: return false
        player.setMedia(song.entity.uri)
        player.play()
        return true
    }

    suspend fun pause() {
        player.pause()
    }

    suspend fun stop() {
        player.stop()
        queue.clear()
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

    suspend fun setSpeed(speed: Float, persist: Boolean) {
        // TODO: implement persist
        player.setSpeed(speed)
    }

    suspend fun setPitch(pitch: Float, persist: Boolean) {
        // TODO: implement persist
        player.setPitch(pitch)
    }

    fun setSleepTimer(duration: Long, quitOnEnd: Boolean = false) {
        val endsAt = System.currentTimeMillis() + duration
        val sleepTimer = SleepTimer(duration = duration, endsAt = endsAt, quitOnEnd = quitOnEnd)
        val timer = Timer()
        timer.schedule(
            kotlin.concurrent.timerTask {
                val shouldQuit = sleepTimer.quitOnEnd
                clearSleepTimer()
                symphony.groove.coroutineScope.launch {
                    pause()
                }
                if (shouldQuit) {
                    symphony.closeApp?.invoke()
                }
            },
            Date.from(Instant.ofEpochMilli(endsAt)),
        )
        clearSleepTimer()
        _sleepTimerTimer = timer
        _sleepTimer.update { sleepTimer }
    }

    fun clearSleepTimer() {
        _sleepTimerTimer?.cancel()
        _sleepTimerTimer = null
        _sleepTimer.update { null }
    }

    fun setPauseOnCurrentSongEnd(value: Boolean) {
        _pauseOnCurrentSongEnd.update { value }
    }

    private fun onCurrentSongEnded() {

    }

    private fun onNextSongChange() {

    }

    private enum class SongEndedReason {
        Finish,
        Exception,
    }

    private fun onSongEnded(source: SongEndedReason) {

    }
}
