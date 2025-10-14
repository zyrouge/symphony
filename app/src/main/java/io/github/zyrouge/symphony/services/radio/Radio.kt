package io.github.zyrouge.symphony.services.radio

import android.net.Uri
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.SymphonyHooks
import io.github.zyrouge.symphony.services.groove.entities.Song
import io.github.zyrouge.symphony.services.groove.entities.SongQueue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.Date
import java.util.Timer

class Radio(private val symphony: Symphony) : SymphonyHooks {
    data class SleepTimer(
        val endsAt: Long,
        val quitOnEnd: Boolean,
    ) {
        data class Internal(val sleepTimer: SleepTimer, val timer: Timer)
    }

    private val queue = RadioQueue(symphony)
    private val player = RadioPlayer(symphony)
    private var sleepTimerInterval: SleepTimer.Internal? = null
    val sleepTimer = MutableStateFlow<SleepTimer?>(null)
    val mediaSessionId get() = player.mediaSessionId

    suspend fun play(): Boolean {
        if (player.hasMedia()) {
            player.play()
            return true
        }
        val songQueue = queue.getCurrentSongQueue() ?: return false
        val songQueueId = songQueue.entity.id
        var song: Song.AlongSongQueueMapping? = null
        var playingTimestamp: Long? = null
        if (songQueue.entity.playingId != null) {
            song = symphony.database.songQueueSongMapping.findById(
                songQueueId,
                songQueue.entity.playingId
            )
            playingTimestamp = songQueue.entity.playingTimestamp
        }
        if (song == null) {
            song = symphony.database.songQueueSongMapping.findHead(songQueue.entity.id)
            playingTimestamp = null
        }
        if (song == null) {
            return false
        }
        play(song.mapping.id, song.entity.uri, playingTimestamp ?: 0L)
        return true
    }

    suspend fun play(songMappingId: String): Boolean {
        val songQueue = queue.getCurrentSongQueue() ?: return false
        val songQueueId = songQueue.entity.id
        val song = symphony.database.songQueueSongMapping.findById(songQueueId, songMappingId)
            ?: return false
        play(songMappingId, song.entity.uri)
        return true
    }

    suspend fun playBySongId(songId: String): Boolean {
        val songQueue = queue.getCurrentSongQueue() ?: return false
        val songQueueId = songQueue.entity.id
        val song = symphony.database.songQueueSongMapping.findBySongId(songQueueId, songId)
            ?: return false
        play(song.mapping.id, song.entity.uri)
        return true
    }

    private suspend fun play(
        songMappingId: String,
        songUri: Uri,
        seek: Long = RadioPlayer.DEFAULT_SEEK,
        speed: Float = RadioPlayer.DEFAULT_SPEED,
        pitch: Float = RadioPlayer.DEFAULT_PITCH,
    ) {
        if (player.getMedia()?.uri == songUri) {
            player.play()
            return
        }
        player.stop()
        val nMedia = RadioPlayer.PlayableMedia(songMappingId, songUri)
        player.setMedia(nMedia)
        player.seek(seek)
        player.setSpeed(speed)
        player.setPitch(pitch)
        player.play()
        queue.updateCurrentSongQueue {
            it.entity.copy(
                playingId = songMappingId,
                isPlaying = false,
                playingTimestamp = seek,
                playingSpeedInt = (speed * SongQueue.SPEED_MULTIPLIER).toInt(),
                playingPitchInt = (pitch * SongQueue.PITCH_MULTIPLIER).toInt(),
            )
        }
    }

    suspend fun pause(): Boolean {
        player.pause()
        return true
    }

    suspend fun stop(): Boolean {
        player.stop()
        queue.clear()
        return true
    }

    suspend fun seek(duration: Long): Boolean {
        player.seek(duration)
        return true
    }

    suspend fun setSpeed(speed: Float, persist: Boolean): Boolean {
        val success = queue.updateCurrentSongQueue {
            val speedInt = (speed * SongQueue.SPEED_MULTIPLIER).toInt()
            val persistedSpeedInt = if (persist) speedInt else it.entity.speedInt
            it.entity.copy(playingSpeedInt = speedInt, speedInt = persistedSpeedInt)
        }
        if (!success) {
            return false
        }
        player.setSpeed(speed)
        return true
    }

    suspend fun setPitch(pitch: Float, persist: Boolean): Boolean {
        val success = queue.updateCurrentSongQueue {
            val pitchInt = (pitch * SongQueue.PITCH_MULTIPLIER).toInt()
            val persistedPitchInt = if (persist) pitchInt else it.entity.pitchInt
            it.entity.copy(playingPitchInt = pitchInt, pitchInt = persistedPitchInt)
        }
        if (!success) {
            return false
        }
        player.setPitch(pitch)
        return true
    }

    suspend fun setSleepTimer(sleepTimer: SleepTimer): Boolean {
        val success = queue.updateCurrentSongQueue {
            it.entity.copy(sleepTimerEndsAt = null)
        }
        if (!success) {
            return false
        }
        val quitOnEnd = false
        val timerTask = kotlin.concurrent.timerTask {
            val shouldQuit = quitOnEnd
            cancelSleepTimer()
            symphony.groove.coroutineScope.launch {
                pause()
            }
            if (shouldQuit) {
                symphony.closeApp?.invoke()
            }
        }
        val timer = Timer()
        timer.schedule(timerTask, Date.from(Instant.ofEpochMilli(sleepTimer.endsAt)))
        cancelSleepTimer()
        sleepTimerInterval = SleepTimer.Internal(sleepTimer = sleepTimer, timer = timer)
        this.sleepTimer.update { sleepTimerInterval?.sleepTimer }
        return true
    }

    fun cancelSleepTimer() {
        sleepTimerInterval?.timer?.cancel()
        sleepTimerInterval = null
        sleepTimer.update { sleepTimerInterval?.sleepTimer }
    }

    suspend fun setPauseOnCurrentSongEnd(value: Boolean) = queue.updateCurrentSongQueue {
        it.entity.copy(pauseOnSongEnd = value)
    }

    suspend fun toggleLoopMode() {
        queue.toggleLoopMode()
    }

    suspend fun setLoopMode(loopMode: SongQueue.LoopMode) {
        queue.setLoopMode(loopMode)
    }

    suspend fun toggleShuffleMode() {
        queue.toggleShuffleMode()
    }

    suspend fun setShuffleMode(to: Boolean) {
        queue.setShuffleMode(to)
    }

    suspend fun add(
        songIds: List<String>,
        position: RadioQueue.AddPosition = RadioQueue.AddPosition.AfterTail,
    ) = queue.add(songIds, position)

    suspend fun add(
        songId: String,
        position: RadioQueue.AddPosition = RadioQueue.AddPosition.AfterTail,
    ) = queue.add(songId, position)

    suspend fun add(
        songs: List<Song>,
        position: RadioQueue.AddPosition = RadioQueue.AddPosition.AfterTail,
    ) = queue.add(songs, position)

    suspend fun add(
        song: Song,
        position: RadioQueue.AddPosition = RadioQueue.AddPosition.AfterTail,
    ) = queue.add(song, position)

    suspend fun remove(songMappingIds: List<String>) = queue.remove(songMappingIds)

    suspend fun remove(songMappingId: String) = queue.remove(songMappingId)

    suspend fun clear(): Boolean {
        stop()
        return queue.clear()
    }

    internal fun onQueueCurrentPlayingSongChanged(song: Song.AlongSongQueueMapping?) {
        symphony.groove.coroutineScope.launch {
            onQueueCurrentPlayingSongChangedNeedsSuspend(song)
        }
    }

    internal suspend fun onQueueCurrentPlayingSongChangedNeedsSuspend(song: Song.AlongSongQueueMapping?) {
//        if (song == null) {
//            player.stop()
//            return
//        }
//        val media = player.getMedia()
//        if (media?.uri == song.entity.uri) {
//            return
//        }
//        play(song.mapping.id, song.entity.uri)
    }

    internal fun onQueueNextPlayingSongChanged(song: Song.AlongSongQueueMapping?) {
        symphony.groove.coroutineScope.launch {
            onQueueNextPlayingSongChangedNeedsSuspend(song)
        }
    }

    internal suspend fun onQueueNextPlayingSongChangedNeedsSuspend(song: Song.AlongSongQueueMapping?) {
        if (song == null) {
            player.setNextMedia(null)
            return
        }
        val nextMedia = player.getNextMedia()
        if (nextMedia?.uri == song.entity.uri) {
            return
        }
        val nNextMedia = RadioPlayer.PlayableMedia(song.mapping.id, song.entity.uri)
        player.setNextMedia(nNextMedia)
    }

    internal enum class SongEndedReason {
        Finish,
        Exception,
    }

    internal fun onPlayerSongEnded(source: SongEndedReason) {
        symphony.groove.coroutineScope.launch {
            onPlayerSongEndedNeedsSuspend(source)
        }
    }

    private suspend fun onPlayerSongEndedNeedsSuspend(source: SongEndedReason) {
//        val songQueue = queue.getCurrentSongQueue() ?: return
//        val queueId = songQueue.entity.id
//        val media = player.getMedia() ?: return
//        val song = symphony.database.songQueueSongMapping.findById(queueId, media.id) ?: return
//        val nextSongMappingId = song.mapping.nextId ?: return
//        val nextSong =
//            symphony.database.songQueueSongMapping.findById(queueId, nextSongMappingId) ?: return
//        val nextMedia = RadioPlayer.PlayableMedia(nextSongMappingId, nextSong.entity.uri)
//        player.setNextMedia(nextMedia)
    }

    internal fun onPlayerIsPlayingChanged(isPlaying: Boolean) {
        symphony.groove.coroutineScope.launch {
            onPlayerIsPlayingChangedNeedsSuspend(isPlaying)
        }
    }

    private suspend fun onPlayerIsPlayingChangedNeedsSuspend(isPlaying: Boolean) {
        queue.updateCurrentSongQueue {
            if (it.entity.isPlaying == isPlaying) {
                return@updateCurrentSongQueue null
            }
            it.entity.copy(isPlaying = isPlaying)
        }
    }

    internal fun onPlayerPlaybackParametersChanged(speed: Float, pitch: Float) {
        symphony.groove.coroutineScope.launch {
            onPlayerPlaybackParametersChangedNeedsSuspend(speed, pitch)
        }
    }

    private suspend fun onPlayerPlaybackParametersChangedNeedsSuspend(speed: Float, pitch: Float) {
        queue.updateCurrentSongQueue {
            val speedInt = (speed * SongQueue.SPEED_MULTIPLIER).toInt()
            val pitchInt = (pitch * SongQueue.PITCH_MULTIPLIER).toInt()
            if (it.entity.speedInt == speedInt || it.entity.pitchInt == pitchInt) {
                return@updateCurrentSongQueue null
            }
            it.entity.copy(speedInt = speedInt, pitchInt = pitchInt)
        }
    }
}
