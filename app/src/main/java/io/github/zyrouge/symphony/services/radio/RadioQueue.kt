package io.github.zyrouge.symphony.services.radio

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.ConcurrentList

enum class RadioLoopMode {
    None,
    Queue,
    Song;

    companion object {
        val all = entries.toTypedArray()
    }
}

class RadioQueue(private val symphony: Symphony) {
    val originalQueue = ConcurrentList<Long>()
    val currentQueue = ConcurrentList<Long>()

    var currentSongIndex = -1
        set(value) {
            field = value
            symphony.radio.onUpdate.dispatch(RadioEvents.QueueIndexChanged)
        }

    var currentShuffleMode = false
        set(value) {
            field = value
            symphony.radio.onUpdate.dispatch(RadioEvents.ShuffleModeChanged)
        }

    var currentLoopMode = RadioLoopMode.None
        set(value) {
            field = value
            symphony.radio.onUpdate.dispatch(RadioEvents.LoopModeChanged)
        }

    val currentSongId: Long?
        get() = getSongIdAt(currentSongIndex)

    fun hasSongAt(index: Int) = index > -1 && index < currentQueue.size
    fun getSongIdAt(index: Int) = if (hasSongAt(index)) currentQueue[index] else null

    fun reset() {
        originalQueue.clear()
        currentQueue.clear()
        currentSongIndex = -1
        symphony.radio.onUpdate.dispatch(RadioEvents.QueueCleared)
    }

    fun add(
        songIds: List<Long>,
        index: Int? = null,
        options: Radio.PlayOptions = Radio.PlayOptions(),
    ) {
        index?.let {
            originalQueue.addAll(it, songIds)
            currentQueue.addAll(it, songIds)
            if (it <= currentSongIndex) {
                currentSongIndex += songIds.size
            }
        } ?: run {
            originalQueue.addAll(songIds)
            currentQueue.addAll(songIds)
        }
        afterAdd(options)
    }

    fun add(
        songId: Long,
        index: Int? = null,
        options: Radio.PlayOptions = Radio.PlayOptions(),
    ) = add(listOf(songId), index, options)

    private fun afterAdd(options: Radio.PlayOptions) {
        if (!symphony.radio.hasPlayer) {
            symphony.radio.play(options)
        }
        symphony.radio.onUpdate.dispatch(RadioEvents.SongQueued)
    }

    fun remove(index: Int) {
        originalQueue.removeAt(index)
        currentQueue.removeAt(index)
        symphony.radio.onUpdate.dispatch(RadioEvents.SongDequeued)
        if (currentSongIndex == index) {
            symphony.radio.play(Radio.PlayOptions(index = currentSongIndex))
        } else if (index < currentSongIndex) {
            currentSongIndex--
        }
    }

    fun remove(indices: List<Int>) {
        var deflection = 0
        var currentSongRemoved = false
        val sortedIndices = indices.sortedDescending()
        for (i in sortedIndices) {
            val index = i - deflection
            originalQueue.removeAt(index)
            currentQueue.removeAt(index)
            when {
                i < currentSongIndex -> deflection++
                i == currentSongIndex -> currentSongRemoved = true
            }
        }
        currentSongIndex -= deflection
        symphony.radio.onUpdate.dispatch(RadioEvents.QueueModified)
        if (currentSongRemoved) {
            symphony.radio.play(Radio.PlayOptions(index = currentSongIndex))
        }
    }

    fun setLoopMode(loopMode: RadioLoopMode) {
        currentLoopMode = loopMode
    }

    fun toggleLoopMode() {
        val next = RadioLoopMode.all.indexOf(currentLoopMode) + 1
        setLoopMode(RadioLoopMode.all[if (next < RadioLoopMode.all.size) next else 0])
    }

    fun toggleShuffleMode() = setShuffleMode(!currentShuffleMode)
    fun setShuffleMode(to: Boolean) {
        currentShuffleMode = to
        val currentSongId = getSongIdAt(currentSongIndex)!!
        currentSongIndex = if (currentShuffleMode) {
            val newQueue = originalQueue.toMutableList()
            newQueue.removeAt(currentSongIndex)
            newQueue.shuffle()
            newQueue.add(0, currentSongId)
            currentQueue.clear()
            currentQueue.addAll(newQueue)
            0
        } else {
            currentQueue.clear()
            currentQueue.addAll(originalQueue)
            currentQueue.indexOfFirst { it == currentSongId }
        }
        symphony.radio.onUpdate.dispatch(RadioEvents.QueueModified)
    }

    fun isEmpty() = originalQueue.isEmpty()

    data class Serialized(
        val currentSongIndex: Int,
        val playedDuration: Long,
        val originalQueue: List<Long>,
        val currentQueue: List<Long>,
        val shuffled: Boolean,
    ) {
        fun serialize() =
            listOf(
                currentSongIndex.toString(),
                playedDuration.toString(),
                originalQueue.joinToString(","),
                currentQueue.joinToString(","),
                shuffled.toString(),
            ).joinToString(";")

        companion object {
            fun create(queue: RadioQueue, playbackPosition: PlaybackPosition) =
                Serialized(
                    currentSongIndex = queue.currentSongIndex,
                    playedDuration = playbackPosition.played,
                    originalQueue = queue.originalQueue.toList(),
                    currentQueue = queue.currentQueue.toList(),
                    shuffled = queue.currentShuffleMode,
                )

            fun parse(data: String): Serialized? {
                try {
                    val semi = data.split(";")
                    return Serialized(
                        currentSongIndex = semi[0].toInt(),
                        playedDuration = semi[1].toLong(),
                        originalQueue = semi[2].split(",").map { it.toLong() },
                        currentQueue = semi[3].split(",").map { it.toLong() },
                        shuffled = semi[4].toBoolean(),
                    )
                } catch (_: Exception) {
                }
                return null
            }
        }
    }

    fun restore(serialized: Serialized) {
        if (serialized.originalQueue.isNotEmpty()) {
            symphony.radio.stop(ended = false)
            originalQueue.clear()
            originalQueue.addAll(serialized.originalQueue)
            currentQueue.clear()
            currentQueue.addAll(serialized.currentQueue)
            symphony.radio.onUpdate.dispatch(RadioEvents.QueueModified)
            currentShuffleMode = serialized.shuffled
            afterAdd(
                Radio.PlayOptions(
                    index = serialized.currentSongIndex,
                    autostart = false,
                    startPosition = serialized.playedDuration,
                )
            )
        }
    }
}
