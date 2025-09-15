package io.github.zyrouge.symphony.services.radio

import android.net.Uri
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.SymphonyHooks
import io.github.zyrouge.symphony.services.groove.entities.Song
import io.github.zyrouge.symphony.services.groove.entities.SongQueue
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.Date
import java.util.Timer

class Radio(private val symphony: Symphony) : SymphonyHooks {
    data class SleepTimer(
        val endsAt: Long,
        val quitOnEnd: Boolean,
        val timer: Timer,
    )

    private val queue = RadioQueue(symphony)
    private val player = RadioPlayer(symphony)
    private var sleepTimer: SleepTimer? = null

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
        player.stop()
        val songQueue = queue.getCurrentSongQueue() ?: return false
        val songQueueId = songQueue.entity.id
        val song = symphony.database.songQueueSongMapping.findById(songQueueId, songMappingId)
            ?: return false
        play(songMappingId, song.entity.uri)
        return true
    }

    private suspend fun play(
        songMappingId: String,
        songUri: Uri,
        seek: Long = 0,
        speed: Float = 1f,
        pitch: Float = 1f,
    ) {
        if (player.getMedia() == songUri) {
            player.play()
            return
        }
        queue.updateCurrentSongQueue {
            it.entity.copy(
                playingId = songMappingId,
                isPlaying = false,
                playingTimestamp = seek,
                playingSpeedInt = (speed * SongQueue.SPEED_MULTIPLIER).toInt(),
                playingPitchInt = (pitch * SongQueue.PITCH_MULTIPLIER).toInt(),
            )
        }
        val media = RadioPlayer.PlayableMedia(songMappingId, songUri)
        player.setMedia(media)
        player.seek(seek)
        player.setSpeed(speed)
        player.setPitch(pitch)
        player.play()
        return
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

    suspend fun setSleepTimer(endsAt: Long?): Boolean {
        val success = queue.updateCurrentSongQueue {
            it.entity.copy(sleepTimerEndsAt = null)
        }
        if (!success) {
            return false
        }
        cancelSleepTimer()
        if (endsAt == null) {
            return true
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
        timer.schedule(timerTask, Date.from(Instant.ofEpochMilli(endsAt)))
        cancelSleepTimer()
        sleepTimer = SleepTimer(endsAt = endsAt, quitOnEnd = quitOnEnd, timer = timer)
        return true
    }

    private fun cancelSleepTimer() {
        sleepTimer?.timer?.cancel()
        sleepTimer = null
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

    internal fun onCurrentSongEnded() {

    }

    internal fun onNextSongChange() {

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
        val songQueue = queue.getCurrentSongQueue() ?: return
        val queueId = songQueue.entity.id
        val media = player.getMedia() ?: return
        val song = symphony.database.songQueueSongMapping.findById(queueId, media.id) ?: return
        val nextSongMappingId = song.mapping.nextId ?: return
        val nextSong =
            symphony.database.songQueueSongMapping.findById(queueId, nextSongMappingId) ?: return
        val nextMedia = RadioPlayer.PlayableMedia(nextSongMappingId, nextSong.entity.uri)
        player.setNextMedia(nextMedia)
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
