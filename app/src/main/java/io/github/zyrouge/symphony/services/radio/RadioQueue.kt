package io.github.zyrouge.symphony.services.radio

import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.services.database.store.SongQueueSongMappingStore
import io.github.zyrouge.symphony.services.groove.entities.Song
import io.github.zyrouge.symphony.services.groove.entities.SongQueue
import io.github.zyrouge.symphony.services.groove.entities.SongQueueSongMapping
import io.github.zyrouge.symphony.utils.lazy_linked_list.LazyLinkedListOperatorHelper

class RadioQueue(private val symphony: Symphony) {
    private class SongQueueSongMappingOperatorEntityFunctions :
        LazyLinkedListOperatorHelper.EntityFunctions<String, SongQueueSongMapping> {
        override fun getEntityId(entity: SongQueueSongMapping) = entity.id
        override fun getEntityNextId(entity: SongQueueSongMapping) = entity.nextId
        override fun getEntityIsHead(entity: SongQueueSongMapping) = entity.isHead

        override fun updateEntityNextId(entity: SongQueueSongMapping, nNextId: String?) =
            entity.copy(nextId = nNextId)

        override fun updateEntityIsHead(entity: SongQueueSongMapping, nIsHead: Boolean) =
            entity.copy(isHead = nIsHead)
    }

    private class SongQueueSongMappingOperatorPersistenceFunctions(
        private val store: SongQueueSongMappingStore,
        private val queueId: String,
    ) :
        LazyLinkedListOperatorHelper.PersistenceFunctions<String, SongQueueSongMapping> {
        override fun getEntitiesByIds(ids: List<String>) = store.entriesByIds(queueId, ids)
            .mapValues { it.value.mapping }

        override fun getEntitiesByNextIds(nextIds: List<String>) =
            store.entriesByNextIds(queueId, nextIds)
                .mapValues { it.value.mapping }

        override fun getHeadEntity() = store.findHead(queueId)?.mapping
        override fun getTailEntity() = store.findByNextId(queueId, null)?.mapping

        override suspend fun insertEntities(entities: List<SongQueueSongMapping>) {
            store.insert(*entities.toTypedArray())
        }

        override suspend fun updateEntities(entities: List<SongQueueSongMapping>) {
            store.update(*entities.toTypedArray())
        }

        override suspend fun deleteEntities(ids: List<String>) {
            store.delete(queueId, ids)
        }
    }

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
        insertAfterId: String? = null,
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
        val operator = createSongQueueSongMappingOperator(queueId)
        operator.append(insertAfterId, songIds) { x, isHead, nextId ->
            SongQueueSongMapping(
                id = symphony.database.songQueueSongMappingIdGenerator.next(),
                queueId = queueId,
                songId = x,
                isHead = isHead,
                nextId = nextId,
                ogNextId = nextId,
            )
        }
        afterAdd(options)
    }

    suspend fun add(
        songId: String,
        insertAfterId: String? = null,
        options: Radio.PlayOptions = Radio.PlayOptions(),
    ) = add(listOf(songId), insertAfterId, options)

    suspend fun add(
        songs: List<Song>,
        insertAfterId: String? = null,
        options: Radio.PlayOptions = Radio.PlayOptions(),
    ) = add(songs.map { it.id }, insertAfterId, options)

    suspend fun add(
        song: Song,
        insertAfterId: String? = null,
        options: Radio.PlayOptions = Radio.PlayOptions(),
    ) = add(listOf(song.id), insertAfterId, options)

    private fun afterAdd(options: Radio.PlayOptions) {
//        if (!symphony.radio.hasPlayer) {
//            symphony.radio.play(options)
//        }
//        symphony.radio.onUpdate.dispatch(Radio.Events.Queue.Modified)
    }

    suspend fun remove(songMappingIds: List<String>): Boolean {
        val queue = symphony.database.songQueue.findByInternalId(SONG_QUEUE_INTERNAL_ID_DEFAULT)
        if (queue == null) {
            return false
        }
        val queueId = queue.entity.id
        val operator = createSongQueueSongMappingOperator(queueId)
        val result = operator.remove(songMappingIds)
        return result.deletedKeys.isNotEmpty()
    }

    suspend fun remove(songMappingId: String) = remove(listOf(songMappingId))

    private suspend fun setLoopMode(queue: SongQueue, loopMode: SongQueue.LoopMode) {
        val nQueue = queue.copy(loopMode = loopMode)
        symphony.database.songQueue.update(nQueue)
    }

    suspend fun setLoopMode(loopMode: SongQueue.LoopMode): Boolean {
        val queue = symphony.database.songQueue.findByInternalId(SONG_QUEUE_INTERNAL_ID_DEFAULT)
        if (queue == null) {
            return false
        }
        setLoopMode(queue.entity, loopMode)
        return true
    }

    suspend fun toggleLoopMode(): Boolean {
        val queue = symphony.database.songQueue.findByInternalId(SONG_QUEUE_INTERNAL_ID_DEFAULT)
        if (queue == null) {
            return false
        }
        val currentLoopMode = queue.entity.loopMode
        val nextLoopModeOrdinal = (currentLoopMode.ordinal + 1) % SongQueue.LoopMode.values.size
        val nextLoopMode = SongQueue.LoopMode.values[nextLoopModeOrdinal]
        setLoopMode(queue.entity, nextLoopMode)
        return true
    }

    fun toggleShuffleMode(): Boolean {
//        val queue = symphony.database.songQueue.findByInternalId(SONG_QUEUE_INTERNAL_ID_DEFAULT)
//        if (queue == null) {
//            return false
//        }
//        val nQueue = queue.entity.copy(shuffled = !queue.entity.shuffled)
//        symphony.database.songQueue.update(nQueue)
        return true
    }

    fun setShuffleMode(to: Boolean) {
//        currentShuffleMode = to
//        if (currentQueue.isNotEmpty()) {
//            val currentSongId = getSongIdAt(currentSongIndex) ?: getSongIdAt(0)!!
//            currentSongIndex = if (currentShuffleMode) {
//                val newQueue = originalQueue.toMutableList()
//                newQueue.removeAt(currentSongIndex)
//                newQueue.shuffle()
//                newQueue.add(0, currentSongId)
//                currentQueue.clear()
//                currentQueue.addAll(newQueue)
//                0
//            } else {
//                currentQueue.clear()
//                currentQueue.addAll(originalQueue)
//                originalQueue.indexOfFirst { it == currentSongId }
//            }
//        }
//        symphony.radio.onUpdate.dispatch(Radio.Events.Queue.Modified)
    }

    private fun createSongQueueSongMappingOperator(queueId: String) = LazyLinkedListOperatorHelper(
        SongQueueSongMappingOperatorEntityFunctions(),
        SongQueueSongMappingOperatorPersistenceFunctions(
            symphony.database.songQueueSongMapping,
            queueId
        )
    )

    companion object {
        const val SONG_QUEUE_INTERNAL_ID_DEFAULT = 1
    }
}
