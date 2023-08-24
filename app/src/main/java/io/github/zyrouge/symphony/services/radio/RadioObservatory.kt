package io.github.zyrouge.symphony.services.radio

import androidx.compose.runtime.mutableStateListOf
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.EventUnsubscribeFn
import io.github.zyrouge.symphony.utils.asList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class RadioObservatory(private val symphony: Symphony) {
    private var updateSubscriber: EventUnsubscribeFn? = null
    private var playbackPositionUpdateSubscriber: EventUnsubscribeFn? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()
    private val _playbackPosition = MutableStateFlow(PlaybackPosition.zero)
    val playbackPosition = _playbackPosition.asStateFlow()
    private val _queueIndex = MutableStateFlow(-1)
    val queueIndex = _queueIndex.asStateFlow()
    private val _queue = mutableStateListOf<Long>()
    val queue = _queue.asList()
    private val _loopMode = MutableStateFlow(RadioLoopMode.None)
    val loopMode = _loopMode.asStateFlow()
    private val _shuffleMode = MutableStateFlow(false)
    val shuffleMode = _shuffleMode.asStateFlow()
    private val _sleepTimer = MutableStateFlow<RadioSleepTimer?>(null)
    val sleepTimer = _sleepTimer.asStateFlow()
    private val _pauseOnCurrentSongEnd = MutableStateFlow<Boolean>(false)
    val pauseOnCurrentSongEnd = _pauseOnCurrentSongEnd.asStateFlow()
    private val _speed = MutableStateFlow(RadioPlayer.DEFAULT_SPEED)
    val speed = _speed.asStateFlow()
    private val _persistedSpeed = MutableStateFlow(RadioPlayer.DEFAULT_SPEED)
    val persistedSpeed = _persistedSpeed.asStateFlow()
    private val _pitch = MutableStateFlow(RadioPlayer.DEFAULT_PITCH)
    val pitch = _pitch.asStateFlow()
    private val _persistedPitch = MutableStateFlow(RadioPlayer.DEFAULT_PITCH)
    val persistedPitch = _persistedPitch.asStateFlow()

    fun start() {
        updateSubscriber = symphony.radio.onUpdate.subscribe { event ->
            when (event) {
                RadioEvents.StartPlaying,
                RadioEvents.StopPlaying,
                RadioEvents.PausePlaying,
                RadioEvents.ResumePlaying -> emitIsPlaying()

                RadioEvents.SongSeeked -> emitPlaybackPosition()
                RadioEvents.SongQueued,
                RadioEvents.SongDequeued,
                RadioEvents.QueueModified,
                RadioEvents.QueueCleared -> emitQueue()

                RadioEvents.QueueIndexChanged -> emitQueueIndex()
                RadioEvents.LoopModeChanged -> emitLoopMode()
                RadioEvents.ShuffleModeChanged -> emitShuffleMode()
                RadioEvents.SongStaged,
                RadioEvents.QueueEnded -> {
                }

                RadioEvents.SleepTimerSet,
                RadioEvents.SleepTimerRemoved -> emitSleepTimer()

                RadioEvents.SpeedChanged -> {
                    emitSpeed()
                    emitPersistedSpeed()
                }

                RadioEvents.PitchChanged -> {
                    emitPitch()
                    emitPersistedPitch()
                }

                RadioEvents.PauseOnCurrentSongEndChanged -> emitPauseOnCurrentSongEnd()
            }
        }
        playbackPositionUpdateSubscriber = symphony.radio.onPlaybackPositionUpdate.subscribe {
            emitPlaybackPosition()
        }
    }

    fun destroy() {
        updateSubscriber?.invoke()
        playbackPositionUpdateSubscriber?.invoke()
    }

    private fun emitIsPlaying() = _isPlaying.tryEmit(symphony.radio.isPlaying)
    private fun emitPlaybackPosition() = _playbackPosition.tryEmit(
        symphony.radio.currentPlaybackPosition ?: PlaybackPosition.zero
    )

    private fun emitQueueIndex() = _queueIndex.tryEmit(symphony.radio.queue.currentSongIndex)
    private fun emitLoopMode() = _loopMode.tryEmit(symphony.radio.queue.currentLoopMode)
    private fun emitShuffleMode() = _shuffleMode.tryEmit(symphony.radio.queue.currentShuffleMode)
    private fun emitSleepTimer() = _sleepTimer.tryEmit(symphony.radio.sleepTimer)
    private fun emitSpeed() = _speed.tryEmit(symphony.radio.currentSpeed)
    private fun emitPersistedSpeed() = _speed.tryEmit(symphony.radio.persistedSpeed)
    private fun emitPitch() = _pitch.tryEmit(symphony.radio.currentPitch)
    private fun emitPersistedPitch() = _pitch.tryEmit(symphony.radio.persistedPitch)
    private fun emitPauseOnCurrentSongEnd() =
        _pauseOnCurrentSongEnd.tryEmit(symphony.radio.pauseOnCurrentSongEnd)

    private fun emitQueue() {
        _queue.clear()
        _queue.addAll(symphony.radio.queue.currentQueue)
    }
}
