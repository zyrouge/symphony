package io.github.zyrouge.symphony.services.radio

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.utils.ConcurrentList
import io.github.zyrouge.symphony.utils.swap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class RadioLoopMode {
    None,
    Queue,
    Song;

    companion object {
        val all = values()
    }
}

class RadioQueue(private val symphony: Symphony) {
    internal val originalQueue = ConcurrentList<Long>()
    internal val currentQueue = ConcurrentList<Long>()
    internal var currentSongIndex = -1
    internal var currentShuffleMode = false
    internal var currentLoopMode = RadioLoopMode.None

    private val _queue = MutableStateFlow(listOf<Long>())
    val queue = _queue.asStateFlow()
    private val _index = MutableStateFlow(currentSongIndex)
    val index = _index.asStateFlow()
    private val _loop = MutableStateFlow(currentLoopMode)
    val loop = _loop.asStateFlow()

    fun hasSongAt(index: Int) = index > -1 && index < currentQueue.size
    fun getSongIdAt(index: Int) = if (hasSongAt(index)) currentQueue[index] else null

    fun reset() {
        originalQueue.clear()
        currentQueue.clear()
        currentSongIndex = -1
        emitQueue()
        emitIndex()
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
        emitQueue()
        emitIndex()
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
    }

    fun remove(index: Int) {
        originalQueue.removeAt(index)
        currentQueue.removeAt(index)
        emitQueue()
        if (currentSongIndex == index) {
            symphony.radio.play(Radio.PlayOptions(index = currentSongIndex))
        } else if (index < currentSongIndex) {
            currentSongIndex--
            emitIndex()
        }
    }

    fun remove(indices: List<Int>) {
        var deflection = 0
        var currentSongRemoved = false
        for (i in indices.sorted()) {
            val index = i - deflection
            originalQueue.removeAt(index)
            currentQueue.removeAt(index)
            when {
                i < currentSongIndex -> deflection++
                i == currentSongIndex -> currentSongRemoved = true
            }
        }
        currentSongIndex -= deflection
        emitQueue()
        emitIndex()
        if (currentSongRemoved) {
            symphony.radio.play(Radio.PlayOptions(index = currentSongIndex))
        }
    }

    fun setLoopMode(loopMode: RadioLoopMode) {
        currentLoopMode = loopMode
        emitLoop()
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
            currentQueue.swap(newQueue)
            0
        } else {
            currentQueue.swap(originalQueue)
            currentQueue.indexOfFirst { it == currentSongId }
        }
        emitQueue()
        emitIndex()
    }

    fun isEmpty() = originalQueue.isEmpty()

    private fun emitQueue() = _queue.tryEmit(currentQueue.toList())
    private fun emitIndex() = _index.tryEmit(currentSongIndex)
    private fun emitLoop() = _loop.tryEmit(currentLoopMode)

    data class Serialized(
        val currentSongIndex: Int,
        val playedDuration: Int,
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
                    originalQueue = queue.originalQueue,
                    currentQueue = queue.currentQueue,
                    shuffled = queue.currentShuffleMode,
                )

            fun parse(data: String): Serialized? {
                try {
                    val semi = data.split(";")
                    return Serialized(
                        currentSongIndex = semi[0].toInt(),
                        playedDuration = semi[1].toInt(),
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
            originalQueue.swap(serialized.originalQueue)
            currentQueue.swap(serialized.currentQueue)
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
