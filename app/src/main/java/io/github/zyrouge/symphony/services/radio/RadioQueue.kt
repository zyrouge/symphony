package io.github.zyrouge.symphony.services.radio

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.groove.entities.Song
import io.github.zyrouge.symphony.services.groove.entities.SongQueue
import io.github.zyrouge.symphony.services.groove.entities.SongQueueSongMapping

class RadioQueue(private val symphony: Symphony) {
//    val queueFlow = symphony.database.songQueue.findFirstAsFlow()
//    val queue = AtomicReference<SongQueue.AlongAttributes?>(null)

//    @OptIn(ExperimentalCoroutinesApi::class)
//    val queueSongsFlow = queueFlow.transformLatest {
//        if (it == null) {
//            emit(emptyList())
//            return@transformLatest
//        }
//        emitAll(symphony.database.songQueueSongMapping.valuesAsFlow(it.entity.id))
//    }
//    val queueSongs = AtomicReference<List<Song>>(emptyList())

    init {
//        symphony.groove.coroutineScope.launch {
//            queueFlow.collect {
//                queue.set(it)
//            }
//        }
//        symphony.groove.coroutineScope.launch {
//            queueSongsFlow.collect {
//                queueSongs.set(it)
//            }
//        }
    }

    suspend fun add(
        songIds: List<String>,
        previousSongMappingId: String? = null,
        options: Radio.PlayOptions = Radio.PlayOptions(),
    ) {
        val origQueue = symphony.database.songQueue.findByInternalId(SONG_QUEUE_INTERNAL_ID_DEFAULT)
        val queueId = origQueue?.entity?.id ?: symphony.database.songQueueIdGenerator.next()
        if (origQueue == null) {
            val queue = SongQueue(
                id = queueId,
                playingId = null,
                playingTimestamp = null,
                playingSpeedInt = SongQueue.SPEED_MULTIPLIER,
                playingPitchInt = SongQueue.PITCH_MULTIPLIER,
                shuffled = false,
                loopMode = SongQueue.LoopMode.None,
                speedInt = SongQueue.SPEED_MULTIPLIER,
                pitchInt = SongQueue.PITCH_MULTIPLIER,
                pauseOnSongEnd = false,
            )
            symphony.database.songQueue.insert(queue)
        }
        var previousSong = previousSongMappingId?.let {
            symphony.database.songQueueSongMapping.findById(queueId, it)
        }
        var nextMappingId = previousSong?.mapping?.nextId
        var ogNextMappingId = previousSong?.mapping?.ogNextId
        val added = mutableListOf<SongQueueSongMapping>()
        var i = 0
        val songIdsCount = songIds.size
        for (x in songIds.reversed()) {
            val isHead = origQueue == null && i == songIdsCount - 1
            val mapping = SongQueueSongMapping(
                id = symphony.database.songQueueSongMappingIdGenerator.next(),
                queueId = queueId,
                songId = x,
                isHead = isHead,
                nextId = nextMappingId,
                ogNextId = ogNextMappingId,
            )
            added.add(mapping)
            nextMappingId = mapping.id
            ogNextMappingId = mapping.id
            i++
        }
        symphony.database.songQueueSongMapping.insert(*added.toTypedArray())
        afterAdd(options)
    }

    suspend fun add(
        songId: String,
        previousSongId: String? = null,
        options: Radio.PlayOptions = Radio.PlayOptions(),
    ) = add(listOf(songId), previousSongId, options)

    suspend fun add(
        songs: List<Song>,
        previousSongId: String? = null,
        options: Radio.PlayOptions = Radio.PlayOptions(),
    ) = add(songs.map { it.id }, previousSongId, options)

    suspend fun add(
        song: Song,
        previousSongId: String? = null,
        options: Radio.PlayOptions = Radio.PlayOptions(),
    ) = add(listOf(song.id), previousSongId, options)

    private fun afterAdd(options: Radio.PlayOptions) {
        if (!symphony.radio.hasPlayer) {
            symphony.radio.play(options)
        }
        symphony.radio.onUpdate.dispatch(Radio.Events.Queue.Modified)
    }

    fun remove(id: String) {
        originalQueue.removeAt(index)
        currentQueue.removeAt(index)
        symphony.radio.onUpdate.dispatch(Radio.Events.Queue.Modified)
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
        symphony.radio.onUpdate.dispatch(Radio.Events.Queue.Modified)
        if (currentSongRemoved) {
            symphony.radio.play(Radio.PlayOptions(index = currentSongIndex))
        }
    }

    fun setLoopMode(loopMode: LoopMode) {
        currentLoopMode = loopMode
    }

    fun toggleLoopMode() {
        val next = (currentLoopMode.ordinal + 1) % LoopMode.values.size
        setLoopMode(LoopMode.values[next])
    }

    fun toggleShuffleMode() = setShuffleMode(!currentShuffleMode)

    fun setShuffleMode(to: Boolean) {
        currentShuffleMode = to
        if (currentQueue.isNotEmpty()) {
            val currentSongId = getSongIdAt(currentSongIndex) ?: getSongIdAt(0)!!
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
                originalQueue.indexOfFirst { it == currentSongId }
            }
        }
        symphony.radio.onUpdate.dispatch(Radio.Events.Queue.Modified)
    }

    companion object {
        const val SONG_QUEUE_INTERNAL_ID_DEFAULT = 1
    }
}
