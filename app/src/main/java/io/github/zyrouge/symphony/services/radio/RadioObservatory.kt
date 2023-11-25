package io.github.zyrouge.symphony.services.radio

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.EventUnsubscribeFn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RadioObservatory(private val symphony: Symphony) {
    private var updateSubscriber: EventUnsubscribeFn? = null
    private var playbackPositionUpdateSubscriber: EventUnsubscribeFn? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()
    private val _playbackPosition = MutableStateFlow(PlaybackPosition.zero)
    val playbackPosition = _playbackPosition.asStateFlow()
    private val _queueIndex = MutableStateFlow(-1)
    val queueIndex = _queueIndex.asStateFlow()
    private val _queue = MutableStateFlow<List<Long>>(emptyList())
    val queue = _queue.asStateFlow()
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
                RadioEvents.ResumePlaying,
                -> emitIsPlaying()

                RadioEvents.SongSeeked -> emitPlaybackPosition()
                RadioEvents.SongQueued,
                RadioEvents.SongDequeued,
                RadioEvents.QueueModified,
                RadioEvents.QueueCleared,
                -> emitQueue()

                RadioEvents.QueueIndexChanged -> emitQueueIndex()
                RadioEvents.LoopModeChanged -> emitLoopMode()
                RadioEvents.ShuffleModeChanged -> emitShuffleMode()
                RadioEvents.SongStaged,
                RadioEvents.QueueEnded,
                -> {
                }

                RadioEvents.SleepTimerSet,
                RadioEvents.SleepTimerRemoved,
                -> emitSleepTimer()

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

    private fun emitIsPlaying() = _isPlaying.update {
        symphony.radio.isPlaying
    }

    private fun emitPlaybackPosition() = _playbackPosition.update {
        symphony.radio.currentPlaybackPosition ?: PlaybackPosition.zero
    }

    private fun emitQueueIndex() = _queueIndex.update {
        symphony.radio.queue.currentSongIndex
    }

    private fun emitLoopMode() = _loopMode.update {
        symphony.radio.queue.currentLoopMode
    }

    private fun emitShuffleMode() = _shuffleMode.update {
        symphony.radio.queue.currentShuffleMode
    }

    private fun emitSleepTimer() = _sleepTimer.update {
        symphony.radio.sleepTimer
    }

    private fun emitSpeed() = _speed.update {
        symphony.radio.currentSpeed
    }

    private fun emitPersistedSpeed() = _persistedSpeed.update {
        symphony.radio.persistedSpeed
    }

    private fun emitPitch() = _pitch.update {
        symphony.radio.currentPitch
    }

    private fun emitPersistedPitch() = _persistedPitch.update {
        symphony.radio.persistedPitch
    }

    private fun emitPauseOnCurrentSongEnd() = _pauseOnCurrentSongEnd.update {
        symphony.radio.pauseOnCurrentSongEnd
    }

    private fun emitQueue() {
        _queue.update {
            symphony.radio.queue.currentQueue.toList()
        }
    }
}
