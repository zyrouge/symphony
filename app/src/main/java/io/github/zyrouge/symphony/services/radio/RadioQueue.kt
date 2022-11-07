package io.github.zyrouge.symphony.services.radio

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.utils.swap

enum class RadioLoopMode {
    None,
    Queue,
    Song;

    companion object {
        val all = values()
    }
}

class RadioQueue(private val symphony: Symphony) {
    val originalQueue = mutableListOf<Long>()
    val currentQueue = mutableListOf<Long>()
    var currentSongIndex = -1
    var currentShuffleMode = false
    var currentLoopMode = RadioLoopMode.None

    val currentPlayingSong: Song?
        get() = if (currentSongIndex != -1) getSongAt(currentSongIndex) else null

    fun hasSongAt(index: Int) = index > -1 && index < currentQueue.size
    fun getSongIdAt(index: Int) = currentQueue[index]
    fun getSongAt(index: Int) = symphony.groove.song.getSongWithId(getSongIdAt(index))

    fun reset() {
        originalQueue.clear()
        currentQueue.clear()
        currentSongIndex = -1
    }

    fun add(
        songIds: List<Long>,
        index: Int? = null,
        options: Radio.PlayOptions = Radio.PlayOptions()
    ) {
        index?.let {
            originalQueue.addAll(it, songIds)
            currentQueue.addAll(it, songIds)
            if (it <= currentSongIndex) {
                currentSongIndex += songIds.size
                symphony.radio.onUpdate.dispatch(RadioEvents.QueueIndexChanged)
            }
        } ?: run {
            originalQueue.addAll(songIds)
            currentQueue.addAll(songIds)
        }
        afterAdd(options)
    }

    @JvmName("addToQueueFromSongList")
    fun add(
        songs: List<Song>,
        index: Int? = null,
        options: Radio.PlayOptions = Radio.PlayOptions()
    ) = add(songs.map { it.id }, index, options)

    fun add(
        song: Song,
        index: Int? = null,
        options: Radio.PlayOptions = Radio.PlayOptions()
    ) = add(song.id, index, options)

    fun add(
        songId: Long,
        index: Int? = null,
        options: Radio.PlayOptions = Radio.PlayOptions()
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
            symphony.radio.onUpdate.dispatch(RadioEvents.QueueIndexChanged)
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
        symphony.radio.onUpdate.dispatch(RadioEvents.QueueModified)
        if (currentSongRemoved) {
            symphony.radio.play(Radio.PlayOptions(index = currentSongIndex))
        }
    }

    fun setLoopMode(loopMode: RadioLoopMode) {
        currentLoopMode = loopMode
        symphony.radio.onUpdate.dispatch(RadioEvents.LoopModeChanged)
    }

    fun toggleLoopMode() {
        val next = RadioLoopMode.all.indexOf(currentLoopMode) + 1
        setLoopMode(RadioLoopMode.all[if (next < RadioLoopMode.all.size) next else 0])
    }

    fun toggleShuffleMode() = setShuffleMode(!currentShuffleMode)
    fun setShuffleMode(to: Boolean) {
        currentShuffleMode = to
        val currentSongId = getSongIdAt(currentSongIndex)
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
        symphony.radio.onUpdate.dispatch(RadioEvents.ShuffleModeChanged)
        symphony.radio.onUpdate.dispatch(RadioEvents.QueueIndexChanged)
    }

    fun isEmpty() = originalQueue.isEmpty()

    data class Serialized(
        val currentSongIndex: Int,
        val playedDuration: Int,
        val originalQueue: List<Long>,
        val currentQueue: List<Long>,
    ) {
        fun serialize() =
            listOf(
                currentSongIndex.toString(),
                playedDuration.toString(),
                originalQueue.joinToString(","),
                currentQueue.joinToString(",")
            ).joinToString(";")

        companion object {
            fun create(queue: RadioQueue, playbackPosition: PlaybackPosition) =
                Serialized(
                    currentSongIndex = queue.currentSongIndex,
                    playedDuration = playbackPosition.played,
                    originalQueue = queue.originalQueue,
                    currentQueue = queue.currentQueue,
                )

            fun parse(data: String): Serialized? {
                try {
                    val semi = data.split(";")
                    return Serialized(
                        currentSongIndex = semi[0].toInt(),
                        playedDuration = semi[1].toInt(),
                        originalQueue = semi[2].split(",").map { it.toLong() },
                        currentQueue = semi[3].split(",").map { it.toLong() },
                    )
                } catch (_: Exception) {
                }
                return null
            }
        }
    }

    fun restore(serialized: Serialized) {
        originalQueue.swap(serialized.originalQueue)
        currentQueue.swap(serialized.currentQueue)
        currentSongIndex = serialized.currentSongIndex
        afterAdd(
            Radio.PlayOptions(
                index = currentSongIndex,
                autostart = false,
                startPosition = serialized.playedDuration
            )
        )
    }
}



